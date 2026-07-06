//
//  TodoBook.cpp
//  booktest
//
//  Created by Gunho Lee on 6/29/16.
//
//

#include "TodoBook.hpp"
#include "Utils/TodoUtil.h"
#include "cocos2d.h"
#include <fstream>
//
//TDBookType TodoBook::getBookType(string &filedata)
//{
//    auto data = TodoUtil::readCSV(filedata);
//
//    for (auto row : data) {
//        if (row.size() < 1) continue;
//        string type = row[0];
//        if (type=="title") {
//            return (row[5] == "text") ? TDBookType::TextOnly : TDBookType::WithAudio;
//        }
//    }
//
//    return TDBookType::Unknown;
//}

string TodoBook::getWordAudioPath(string wordAudio)
{
    if (wordAudio.empty() || wordAudio=="-") return "";

    auto wordAudioPath = filePrefix+"word/"+wordAudio;

    if (FileUtils::getInstance()->isFileExist(wordAudioPath)) {
        return wordAudioPath;
    }

    wordAudioPath = commonPrefix+"word/"+wordAudio;
    if (FileUtils::getInstance()->isFileExist(wordAudioPath)) {
        return wordAudioPath;
    }

    // Translated books reference the sentence/page audio as the word audio;
    // that clip lives under page/ rather than word/.
    wordAudioPath = filePrefix+"page/"+wordAudio;
    if (FileUtils::getInstance()->isFileExist(wordAudioPath)) {
        return wordAudioPath;
    }

    return "";
}

string TodoBook::normalizeWordForAudio(const string& wordText)
{
    string out;
    for (char c : wordText) {
        if (c>='A' && c<='Z') c = c - 'A' + 'a';
        if ((c>='a' && c<='z') || (c>='0' && c<='9') || c=='-') out += c;
    }
    // strip leading/trailing hyphens
    size_t b = out.find_first_not_of('-');
    size_t e = out.find_last_not_of('-');
    if (b==string::npos) return "";
    return out.substr(b, e-b+1);
}

string TodoBook::getWordAudioPathForWord(const string& wordText, const string& fallbackFilename)
{
    // 1) Prefer a genuine per-word clip the book already references
    //    (e.g. alphabet books ship their own word/ recordings). Only look in
    //    word/ dirs here, NOT page/ (page/ holds the whole-sentence audio).
    if (!fallbackFilename.empty() && fallbackFilename != "-") {
        string p = filePrefix + "word/" + fallbackFilename;
        if (FileUtils::getInstance()->isFileExist(p)) return p;
        p = commonPrefix + "word/" + fallbackFilename;
        if (FileUtils::getInstance()->isFileExist(p)) return p;
    }

    // 2) Generated per-word Malay clip (ms_<word>.m4a) from the shared pool.
    //    This is what makes tapping a word in a translated story book speak
    //    that word (their word entries otherwise point at sentence audio).
    string n = normalizeWordForAudio(wordText);
    if (!n.empty()) {
        string fn = "ms_" + n + ".m4a";
        // sibling "common/word/" computed without ".." (asset-manager safe)
        string base = filePrefix;
        if (!base.empty() && base.back()=='/') base.pop_back();
        size_t slash = base.find_last_of('/');
        string parent = (slash==string::npos) ? "" : base.substr(0, slash+1);
        string candidates[] = {
            filePrefix + "word/" + fn,          // this book's own word pool
            parent + "common/word/" + fn,       // shared pool (actual lowercase dir)
            commonPrefix + "word/" + fn,        // ../Common/ fallback
        };
        for (auto& c : candidates) {
            if (FileUtils::getInstance()->isFileExist(c)) return c;
        }
    }

    // 3) Nothing per-word: fall back to the sentence/page audio.
    return getWordAudioPath(fallbackFilename);
}


void TodoBook::readData(string &filedata)
{
    auto data = TodoUtil::readCSV(filedata);
    
    fontSize = -1;
    
    for (auto row : data)
    {
        if (row.size() < 1) continue;
        string type = row[0];
        if (type.front()=='#') continue;
        
        
        if (type == "title")
        {
            bookTitle = row[1];
            titleAudioFilename = row[2];
            titleImageFilename = row[3];
            
            auto layout = row[4];
            if (layout == "portrait") bookLayout = TDBookLayout::Portrait;
            else if (layout == "portrait_traditional") bookLayout = TDBookLayout::Portrait_Traditional;
            else if (layout == "square") bookLayout = TDBookLayout::Square;
            else if (layout == "landscape") bookLayout = TDBookLayout::Landscape;
            else bookLayout = TDBookLayout::Landscape;
            bookType = (row[5] == "text") ? TDBookType::TextOnly : TDBookType::WithAudio;
            
            if (row.size()>6) fontSize = TodoUtil::stoi(row[6]);
        }
        else if (type == "page")
        {
            TodoPage page;
            page.pageNum = TodoUtil::stoi(row[1]);
            page.pageImageFilename = row[2];
            page.wordwrap = (row[3]=="wordwrap") ? true : false;
            page.creditPage = false;
            page.lastPage = false;
            page.fontSize = fontSize;
            pages.push_back(page);
            
        }
        else if (type == "paragraph")
        {
            TodoPage &currentPage = pages.back();
            
            TodoParagraph paragraph;
            currentPage.paragraphs.push_back(paragraph);
            
        }
        else if (type == "sentence")
        {
            TodoPage &currentPage = pages.back();
            TodoParagraph &currentParagraph = currentPage.paragraphs.back();
            TodoSentence sentence;
            
            if (bookType == TDBookType::TextOnly)
            {
                
                auto ws = TodoUtil::split(row[3], ' ');
                for (auto w : ws) {
                    TodoWord word;
                    word.word = w;
                    sentence.words.push_back(word);
                }
                
            }
            else
            {
                sentence.sentenceAudioFilename = row[1];
                sentence.startTimingInPage = TodoUtil::stod(row[2]);
            }
            
            currentParagraph.sentences.push_back(sentence);
        }
        else if (type == "word")
        {
            if (bookType == TDBookType::WithAudio)
            {
                TodoPage &currentPage = pages.back();
                TodoParagraph &currentParagraph = currentPage.paragraphs.back();
                TodoSentence &currentSentence = currentParagraph.sentences.back();
                
                TodoWord word;
                word.startTimingInSentence = TodoUtil::stod(row[1]);
                word.startTimingInPage = currentSentence.startTimingInPage + word.startTimingInSentence;
                word.endTimingInSentence = TodoUtil::stod(row[2]);
                word.endTimingInPage = currentSentence.startTimingInPage + word.endTimingInSentence;
                word.word = row[3];
                word.wordAudioFilename = row[4];
                word.wordAudioLength = 0;
                if (row.size()>5) {
                    word.wordAudioLength = TodoUtil::stod(row[5]);
                }
                
                if (word.wordAudioLength<0.001) {
                    word.wordAudioLength = word.endTimingInSentence - word.startTimingInSentence + 0.2;
                }

                // NB: A "word" entry may actually hold a whole sentence/phrase
                // (e.g. translated books store one block per page). Split it into
                // individual tokens so the layout can wrap across multiple lines
                // at the same font size. All tokens share the same timing + audio,
                // so they highlight together and tapping any plays the same audio.
                auto tokens = TodoUtil::split(word.word, ' ');
                if (tokens.size() <= 1) {
                    currentSentence.words.push_back(word);
                } else {
                    for (auto &tk : tokens) {
                        if (tk.empty()) continue;
                        TodoWord sub = word;
                        sub.word = tk;
                        currentSentence.words.push_back(sub);
                    }
                }
            }
        }
    }
}



void TodoBook::readCredit(string &filedata)
{
    
    TodoPage page;
    page.pageNum = pages.size();
    page.pageImageFilename = "";
    page.wordwrap = false;
    page.creditPage = true;
    
    TodoParagraph titleP, creditP, licenseP;
    
    auto makeSentence = [&](string& s){
        TodoSentence sentence;
        
        TodoWord word;
        word.word = s;
        
        sentence.words.push_back(word);
        return sentence;
        
    };
    
    titleP.sentences.push_back(makeSentence(this->bookTitle));
    
    std::istringstream iss(filedata);
    std::string line;
    
    bool isCredit = false;
    bool isLicense = false;
    
    
    while(iss.good())
    {
        TodoUtil::safegetline(iss, line);
        if(line.length() == 0)
        {
            if (!iss.good()) break;
            continue;
        }

        if (line.find("#credit")==0) {
            isCredit = true;
            isLicense = false;
            continue;
        }
        
        if (line.find("#license")==0) {
            isCredit = false;
            isLicense = true;
            continue;
        }
        
        if (isCredit) creditP.sentences.push_back(makeSentence(line));
        if (isLicense) licenseP.sentences.push_back(makeSentence(line));

    }
    
    page.paragraphs.push_back(titleP);
    page.paragraphs.push_back(creditP);
    page.paragraphs.push_back(licenseP);
    
    pages.push_back(page);
    
}


bool TodoBook::readFile(string prefix, string filename)
{
    string bookdataPath = prefix + filename;
    string bookdata = cocos2d::FileUtils::getInstance()->getStringFromFile(bookdataPath);
    
    if (bookdata.length() == 0) return false;
    
    filePrefix = prefix;
    commonPrefix = prefix + "../Common/";
    imagePrefix = prefix+pagePrefix;
    
    readData(bookdata);
    if(pages.size() > 0) pages.back().lastPage = true;
    
    string creditPath = prefix + "credit.txt";
    string creditData = cocos2d::FileUtils::getInstance()->getStringFromFile(creditPath);
    readCredit(creditData);
    
    
    return true;
}


bool TodoBook::writeFile(string prefix, string filename)
{
   
    std::string writeString = "";

    std::vector<std::string> row;
    row.clear();
    
    // title
    {
        row.push_back("title");
        row.push_back(bookTitle);
        row.push_back(titleAudioFilename);
        row.push_back(titleImageFilename);
        switch (bookLayout) {
            case TDBookLayout::Portrait:
                row.push_back("portrait");
                break;
            case TDBookLayout::Portrait_Traditional:
                row.push_back("portrait_traditional");
                break;
            case TDBookLayout::Square:
                row.push_back("square");
                break;
            case TDBookLayout::Landscape:
                row.push_back("landscape");
                break;
        }
        
        switch(bookType) {
            case TDBookType::TextOnly:
                row.push_back("text");
                break;
            case TDBookType::WithAudio:
                row.push_back("audio");
                break;
        }
        
        if (fontSize>0) row.push_back(TodoUtil::dtos(fontSize));
        
        writeString += TodoUtil::combineCSV(row) + "\n";
        row.clear();
    }
    
    
    //page
    for (auto page : pages) {
        if (page.creditPage) continue;
        row.push_back("page");
        row.push_back(TodoUtil::itos(page.pageNum));
        row.push_back(page.pageImageFilename);
        row.push_back(page.wordwrap ? "wordwrap" : "");
        writeString += TodoUtil::combineCSV(row) + "\n";
        row.clear();
        
        for (auto para : page.paragraphs) {
            writeString += "paragraph\n";
            
            for (auto sen : para.sentences) {
                row.push_back("sentence");
                
                if (bookType==TDBookType::TextOnly) {
                    row.push_back("");
                    row.push_back("");
                    string sentenceString = "";
                    
                    for (auto w : sen.words) {
                        sentenceString += w.word + " ";
                    }
                    row.push_back(TodoUtil::trim(sentenceString));
                    writeString += TodoUtil::combineCSV(row) + "\n";
                    row.clear();
                } else if (bookType==TDBookType::WithAudio) {
                    row.push_back(sen.sentenceAudioFilename);
                    row.push_back(TodoUtil::dtos(sen.startTimingInPage));
                    writeString += TodoUtil::combineCSV(row) + "\n";
                    row.clear();
                    
                    for (auto w : sen.words) {
                        row.push_back("word");
                        row.push_back(TodoUtil::dtos(w.startTimingInSentence));
                        row.push_back(TodoUtil::dtos(w.endTimingInSentence));
                        row.push_back(w.word);
                        row.push_back(w.wordAudioFilename);
                        row.push_back(TodoUtil::dtos(w.wordAudioLength));
                        writeString += TodoUtil::combineCSV(row) + "\n";
                        row.clear();
                        
                        
                    }
                }
            }
        }
        
    }
    
    
    string bookdataPath = filePrefix + filename;
            
    bool ret = FileUtils::getInstance()->writeStringToFile(writeString, bookdataPath);
    return ret;
    
    
}



string TodoBook::checkData()
{
    string ret = "";
    
    auto checkImage = [&ret, this](string filename, string msg) {
        auto path = imagePrefix + filename;
        if (FileUtils::getInstance()->isFileExist(path)) return;
        path = imagePrefix + filename + ".png";
        if (FileUtils::getInstance()->isFileExist(path)) return;
        path = imagePrefix + filename + ".jpg";
        if (FileUtils::getInstance()->isFileExist(path)) return;
        ret+= msg + " ["+filename+"]\n";
    };
    

    checkImage(titleImageFilename, "missing title image");
//    auto titleImage = imagePrefix + titleImageFilename;
//    if (!FileUtils::getInstance()->isFileExist(titleImage)) ret+= "missing title image ["+titleImageFilename+"]\n";
    
    auto titleAudio = imagePrefix + titleAudioFilename;
    if (!FileUtils::getInstance()->isFileExist(titleAudio)) ret+= "missing title audio ["+titleAudioFilename+"]\n";
    
    for (auto p : pages) {
        checkImage(p.pageImageFilename, "missing page image");

//        auto pageImage = imagePrefix + p.pageImageFilename;
//        if (!FileUtils::getInstance()->isFileExist(pageImage)) ret += "missing page image ["+p.pageImageFilename+"]\n";
        
        for (auto para : p.paragraphs) {
            for (auto s : para.sentences) {
                
                auto audioPath = filePrefix+"page/"+s.sentenceAudioFilename;
                if (!FileUtils::getInstance()->isFileExist(audioPath)) ret += "missing sentence audio ["+s.sentenceAudioFilename+"]\n";
                
                for (auto w : s.words) {
                    if (w.wordAudioFilename=="-") continue;
                    if (getWordAudioPath(w.wordAudioFilename).empty()) {
                        ret += "missing word audio ["+w.wordAudioFilename+"]\n";
                    }
                }
                
                
            }
        }
    }
    
    return ret;
    
}


