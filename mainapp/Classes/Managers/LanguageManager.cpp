//
//  LanguageManager.cpp
//  KitkitSchool
//
//  Created by Sungwoo Kang on 6/30/16.
//
//

#include "LanguageManager.hpp"
#include "cocos2d.h"
#include <utility>

USING_NS_CC;
using namespace std;

LanguageManager* LanguageManager::_instance = 0;

LanguageManager* LanguageManager::getInstance()
{
    if(!_instance) {
        _instance = new LanguageManager();
        _instance->init();
    }
    return _instance;
}


void LanguageManager::init()
{
#if false
    auto defaultLang = LanguageType::ENGLISH;
#endif
    //auto defaultLang = LanguageType::SWAHILI;
    auto defaultLang = "en-US";//UserDefault::getInstance()->getStringForKey("appLanguage", "en") == "en" ? "en-US" : "sw-TZ";

    auto localeCode = UserDefault::getInstance()->getStringForKey("LocaleCode", defaultLang);

    auto localeType = convertLocaleCodeToType(localeCode);
    if (localeType>=LocaleType_MAX) localeType = sw_TZ;
    
    
    _supportedLocales.clear();
    for (int i=0; i<LocaleType_MAX; i++) {
        LocaleType l = (LocaleType)i;
        auto lc = convertLocaleTypeToCode(l);
        auto lp = "Localized/"+lc+"/CurriculumData.tsv";
        if (FileUtils::getInstance()->isFileExist(lp)) _supportedLocales.push_back(l);
        
    }
    if (_supportedLocales.size() == 0) {
        CCLOGERROR("No curriculumdata.tsv is found for any language. check %s/KitkitSchool/location.txt, which is curretnly refers to %s",
                   FileUtils::getInstance()->getWritablePath().c_str(),
                   FileUtils::getInstance()->getDefaultResourceRootPath().c_str());
        exit(1);
    } else{
        if (std::find(_supportedLocales.begin(), _supportedLocales.end(), localeType)==_supportedLocales.end())
            localeType = _supportedLocales.front();
    }
    
    

    setCurrentLocale(localeType);

    
    initLocalizationMap();
    
    
    
}

LanguageManager::LocaleType LanguageManager::convertLocaleCodeToType(std::string localeCode)
{
    if (localeCode.length()<5) return LocaleType_MAX;
    auto lang = localeCode.substr(0, 2);
    auto region = localeCode.substr(3, 2);
    
    if (lang=="en") {
        if (region=="US") return en_US;
        if (region=="KE") return en_KE;
        if (region=="GB") return en_GB;
    } else if (lang=="sw") {
        if (region=="TZ") return sw_TZ;
    } else if (lang=="my") {
        if (region=="MY") return ms_MY;
    }
    
    return LocaleType_MAX;
}

std::string LanguageManager::convertLocaleTypeToCode(LanguageManager::LocaleType localeType)
{
    switch (localeType) {
        case en_US: return "en-US"; break;
        case en_GB: return "en-GB"; break;
        case en_KE: return "en-KE"; break;
        case sw_TZ: return "sw-TZ"; break;
        case ms_MY: return "ms-MY"; break;
        default: break;
    }
    
    return "sw-TZ";
    
}

void LanguageManager::setCurrentLocale(LocaleType type)
{
    bool dirty = false;
    if (_currentLocale != type) dirty = true;
    
    
    _currentLocale = type;

    if (dirty) {
        auto langCode = convertLocaleTypeToCode(type);
        UserDefault::getInstance()->setStringForKey("LocaleCode", langCode);
        UserDefault::getInstance()->flush();
    }
    
    _localizedResourcePaths.clear();
    switch (_currentLocale) {
        default:
            CCLOGERROR("No proper language is found in %s", __PRETTY_FUNCTION__);
            // fall through
        case sw_TZ: _localizedResourcePaths = { "sw-tz" }; break;
        case en_US: _localizedResourcePaths = { "en-us" }; break;
        case en_GB: _localizedResourcePaths = { "en-gb", "en-us" }; break;
        case en_KE: _localizedResourcePaths = { "en-ke", "en-us" }; break;
        case ms_MY: _localizedResourcePaths = { "ms-my" }; break;
            
    }

    
    std::vector<std::string> paths = {};
    
    for (auto p : _localizedResourcePaths) {
        auto localizedPath = "localized/"+p;
        paths.push_back(localizedPath);
        paths.push_back(localizedPath+"/games");
    }
    paths.push_back("games");
    paths.push_back("main");
    
    
    FileUtils::getInstance()->setSearchPaths(paths);
    
    
}

LanguageManager::LocaleType LanguageManager::getCurrentLocaleType()
{
    return _currentLocale;
}

LanguageManager::LocaleType LanguageManager::findNextLocale()
{
    LocaleType next = (LocaleType)((int)_currentLocale+1);
  
    while (next!=_currentLocale) {
        if (std::find(_supportedLocales.begin(), _supportedLocales.end(), next)!=_supportedLocales.end()) return next;
        next = (LocaleType)((int)next+1);
        if (next>=LocaleType_MAX) next = (LocaleType)0;
        
    }
    
    return _currentLocale;
}


std::string LanguageManager::getCurrentLanguageCode()
{
    
    std::string langCode = convertLocaleTypeToCode(_currentLocale);
    return langCode.substr(0, 2);
    
}

std::string LanguageManager::getCurrentLanguageTag()
{
    return getCurrentLocaleCode();
}

std::string LanguageManager::getCurrentLocaleCode()
{
    std::string langCode=convertLocaleTypeToCode(_currentLocale);
    return langCode;
}


std::string LanguageManager::soundPathForWordFile(std::string& wordFile)
{
    std::string folder;
    
//    switch (_langType) {
//        case ENGLISH: folder = "Common/Sounds/Pam.en_US/"; break;
//        case SWAHILI: folder = "Common/Sounds/Imma.sw_TZ/"; break;
//    }
    
    std::string path = findLocalizedResource("LetterVoice/"+wordFile);
    if (path!="") return path;
    path = findLocalizedResource("WordVoice/"+wordFile);
    if (path!="") return path;
    
    return "";
    
}

std::string LanguageManager::getLocalizedString(std::string str)
{
    
    std::string localized;
    
    switch (_currentLocale) {
        case en_US: localized = _localizationMapEnglish[str]; break;
        case sw_TZ: localized = _localizationMapSwahili[str]; break;
        case ms_MY: localized = _localizationMapMalay[str]; break;
    }
    
    if (localized.empty()) return str;
    
    return localized;
    
}

std::string LanguageManager::findLocalizedResource(std::string path)
{
    
    // handled by Cocos...
    
    return path;

//    
//    for (auto p : _localizedResourcePaths) {
//        auto localizedPath = "Localized/"+p+"/"+path;
//        if (FileUtils::getInstance()->isFileExist(localizedPath)) return localizedPath;
//    }
//
//
//    return "";
}

void LanguageManager::initLocalizationMap()
{
    _localizationMapEnglish["Stop the test"] = "Stop the test";
    _localizationMapEnglish["Go back to test"] = "Go back to test";
    
    // Malay translations
    _localizationMapMalay["Great!"] = "Hebat!";
    _localizationMapMalay["Are you ready for"] = "Adakah anda bersedia untuk";
    _localizationMapMalay["Prove it!"] = "Buktikan!";
    _localizationMapMalay["Try and get 8 questions correct!"] = "Cuba dan dapatkan 8 soalan yang betul!";
    _localizationMapMalay["Challenge"] = "Cabaran";
    _localizationMapMalay["Congratulations!"] = "Tahniah!";
    _localizationMapMalay["You passed!"] = "Anda lulus!";
    _localizationMapMalay["You failed"] = "Anda gagal";
    _localizationMapMalay["Practice more and try again later."] = "Berlatih lebih banyak dan cuba lagi nanti.";
    _localizationMapMalay["Success!"] = "Kejayaan!";
    _localizationMapMalay["You are not ready."] = "Anda belum bersedia.";
    _localizationMapMalay["You need more practice."] = "Anda perlu lebih banyak latihan.";
    _localizationMapMalay["Welcome!"] = "Selamat datang!";
    _localizationMapMalay["Start"] = "Mula";
    _localizationMapMalay["Next"] = "Seterusnya";
    _localizationMapMalay["Back"] = "Kembali";
    _localizationMapMalay["OK"] = "OK";
    _localizationMapMalay["Error"] = "Ralat";
    _localizationMapMalay["Enter"] = "Masuk";
    _localizationMapMalay["Clear"] = "Kosongkan";
    _localizationMapMalay["English"] = "Bahasa Inggeris";
    _localizationMapMalay["Math"] = "Matematik";
    
    _localizationMapSwahili["Great!"] = "Vizuri!";
    
    _localizationMapSwahili["Are you ready for"] = "Je, uko tayari kwa";
    _localizationMapSwahili["Prove it!"] = "Thibitisha!";
    _localizationMapSwahili["Try and get 8 questions correct!"] = "Jaribu na toa majibu sahihi manane!";
    _localizationMapSwahili["Challenge"] = "Jaribu";
    _localizationMapSwahili["Congratulations!"] = "Hongera!";
    _localizationMapSwahili["You passed!"] = "Umefaulu!";
    _localizationMapSwahili["You failed"] = "Umeshindwa";
    _localizationMapSwahili["Practice more and try again later."] = "Fanya mazoezi zaidi na rudi baadae.";
    _localizationMapSwahili["Success!"] = "Mafanikio!";
    _localizationMapSwahili["You are not ready."] = "Hauko tayari.";
    _localizationMapSwahili["You need more practice."] = "Unahitaji mazoezi zaidi.";
    _localizationMapSwahili["Welcome!"] = "Karibu!";
    
    _localizationMapSwahili["Start"] = "Anza";
    _localizationMapSwahili["Next"] = "Nenda mbele";
    _localizationMapSwahili["Back"] = "Rudi nyuma";
    _localizationMapSwahili["OK"] = "OK";
    _localizationMapSwahili["Error"] = "Hitilafu";
    _localizationMapSwahili["Enter"] = "Chomeka";
    _localizationMapSwahili["Clear"] = "Futa";
    
    _localizationMapSwahili["English"] = "Kiswahili";
    _localizationMapSwahili["Math"] = "Hesabu";
    
    _localizationMapEnglish["TutorialTrace"] = "Line Tracing";
    _localizationMapSwahili["TutorialTrace"] = "Kufuatisha Mstari";
    _localizationMapMalay["TutorialTrace"] = "Menggaris Garisan";
    
    _localizationMapEnglish["FindTheMatch"] = "Find the Pair";
    _localizationMapSwahili["FindTheMatch"] = "Tafuta Sare";
    _localizationMapMalay["FindTheMatch"] = "Cari Pasangan";
    
    _localizationMapEnglish["NumberMatching"] = "Number Matching";
    _localizationMapSwahili["NumberMatching"] = "Kufananisha Nambari";
    _localizationMapMalay["NumberMatching"] = "Padanan Nombor";
    
    _localizationMapEnglish["Tapping"] = "Bubble Pop";
    _localizationMapSwahili["Tapping"] = "Pasua Povu la Sabuni";
    _localizationMapMalay["Tapping"] = "Letupkan Buih";
    
    _localizationMapEnglish["LetterMatching"] = "Literacy Matching";
    _localizationMapSwahili["LetterMatching"] = "Kufananisha Kusoma na Kuandika";
    _localizationMapMalay["LetterMatching"] = "Padanan Huruf";
    
    _localizationMapEnglish["AnimalPuzzle"] = "Animal Puzzle";
    _localizationMapSwahili["AnimalPuzzle"] = "Fumbo la Picha";
    _localizationMapMalay["AnimalPuzzle"] = "Teka-teki Haiwan";
    
    _localizationMapEnglish["PatternTrain"] = "Pattern Train";
    _localizationMapSwahili["PatternTrain"] = "Reli ya Garimoshi";
    _localizationMapMalay["PatternTrain"] = "Kereta Api Corak";
    
    _localizationMapEnglish["Video"] = "Video";
    _localizationMapSwahili["Video"] = "Video";
    _localizationMapMalay["Video"] = "Video";
    
    _localizationMapEnglish["Counting"] = "Counting";
    _localizationMapSwahili["Counting"] = "Kuhesabu";
    _localizationMapMalay["Counting"] = "Mengira";
    
    _localizationMapEnglish["EquationMaker"] = "Equation Maker";
    _localizationMapSwahili["EquationMaker"] = "Kiumba Mlinganyo";
    _localizationMapMalay["EquationMaker"] = "Pembuat Persamaan";
    
    _localizationMapEnglish["NumberTrain"] = "Number Train";
    _localizationMapSwahili["NumberTrain"] = "Nambari ya Garimoshi";
    _localizationMapMalay["NumberTrain"] = "Kereta Api Nombor";
    
    _localizationMapEnglish["AlphabetPuzzle"] = "Alphabet Puzzle";
    _localizationMapSwahili["AlphabetPuzzle"] = "Fumbo la Alfabeti";
    _localizationMapMalay["AlphabetPuzzle"] = "Teka-teki Abjad";
    
    _localizationMapEnglish["Book"] = "Book";
    _localizationMapSwahili["Book"] = "Kitabu";
    _localizationMapMalay["Book"] = "Buku";
    
    _localizationMapEnglish["Comprehension"] = "Comprehension Questions";
    _localizationMapSwahili["Comprehension"] = "Maswali ya Ufahamu";
    _localizationMapMalay["Comprehension"] = "Soalan Kefahaman";
    
    _localizationMapEnglish["DoubleDigit"] = "Double Digit Math";
    _localizationMapSwahili["DoubleDigit"] = "Hisabati ya Tarakimu Mbili";
    _localizationMapMalay["DoubleDigit"] = "Matematik Digit Ganda";
    
    _localizationMapEnglish["FishTank"] = "Fish Tank";
    _localizationMapSwahili["FishTank"] = "Tangi ya Samaki";
    _localizationMapMalay["FishTank"] = "Tangki Ikan";
    
    _localizationMapEnglish["HundredPuzzle"] = "100 Puzzle";
    _localizationMapSwahili["HundredPuzzle"] = "Fumbo la Nambari 100";
    _localizationMapMalay["HundredPuzzle"] = "Teka-teki 100";
    
    _localizationMapEnglish["LetterTrace"] = "Letter Tracing";
    _localizationMapSwahili["LetterTrace"] = "Kufuatisha Herufi";
    _localizationMapMalay["LetterTrace"] = "Menggaris Huruf";
    
    _localizationMapEnglish["MovingInsects"] = "Bug Math";
    _localizationMapSwahili["MovingInsects"] = "Mchezo wa Mdudu";
    _localizationMapMalay["MovingInsects"] = "Matematik Serangga";
    
    _localizationMapEnglish["SentenceMaker"] = "Sentence Maker";
    _localizationMapSwahili["SentenceMaker"] = "Kiumba Sentensi";
    _localizationMapMalay["SentenceMaker"] = "Pembuat Ayat";
    
    _localizationMapEnglish["ShapeMatching"] = "Shape Matching";
    _localizationMapSwahili["ShapeMatching"] = "Kufananisha Maumbo";
    _localizationMapMalay["ShapeMatching"] = "Padanan Bentuk";
    
    _localizationMapEnglish["SoundTrain"] = "Sound Train";
    _localizationMapSwahili["SoundTrain"] = "Sauti ya Garimoshi";
    _localizationMapMalay["SoundTrain"] = "Kereta Api Bunyi";
    
    _localizationMapEnglish["Spelling"] = "Spelling";
    _localizationMapSwahili["Spelling"] = "Matamshi";
    _localizationMapMalay["Spelling"] = "Ejaan";
    
    _localizationMapEnglish["WordTracing"] = "Word Tracing";
    _localizationMapSwahili["WordTracing"] = "Kufuatisha Maneno";
    _localizationMapMalay["WordTracing"] = "Menggaris Perkataan";
    
    _localizationMapEnglish["NumberTracing"] = "Learn to 10";
    _localizationMapSwahili["NumberTracing"] = "Jifunze Mpaka 10";
    _localizationMapMalay["NumberTracing"] = "Belajar hingga 10";
    
    _localizationMapEnglish["StarFall"] = "Typing";
    _localizationMapSwahili["StarFall"] = "Kuchapa";
    _localizationMapMalay["StarFall"] = "Mengetik";
    
    _localizationMapEnglish["WordMachine"] = "Word Machine";
    _localizationMapSwahili["WordMachine"] = "Mashine ya Maneno";
    _localizationMapMalay["WordMachine"] = "Mesin Perkataan";
    
    _localizationMapEnglish["NumberTracingExt"] = "Number Tracing";
    _localizationMapSwahili["NumberTracingExt"] = "Kufuatisha Nambari";
    _localizationMapMalay["NumberTracingExt"] = "Menggaris Nombor";
    
    _localizationMapEnglish["LetterTracingCard"] = "Trace 3 Times";
    _localizationMapSwahili["LetterTracingCard"] = "Fuatisha Mara 3";
    _localizationMapMalay["LetterTracingCard"] = "Garis 3 Kali";
    
    _localizationMapEnglish["NumberPuzzle"] = "Number Blocks";
    _localizationMapSwahili["NumberPuzzle"] = "Fumbo la Nambari";
    _localizationMapMalay["NumberPuzzle"] = "Blok Nombor";
    
    _localizationMapEnglish["Arrange the numbers in order from smallest to largest"] = "Arrange the numbers in order from smallest to largest";
    _localizationMapSwahili["Arrange the numbers in order from smallest to largest"] = "panga kwa mpangilio kutoka ndogo zaidi kwenda kubwa zaidi";
    _localizationMapMalay["Arrange the numbers in order from smallest to largest"] = "Susun nombor dari terkecil hingga terbesar";

    _localizationMapEnglish["Largest number"] = "Largest number";
    _localizationMapSwahili["Largest number"] = "Namba ipi ndiyo kubwa zaidi?";
    _localizationMapMalay["Largest number"] = "Nombor terbesar";
    
    _localizationMapEnglish["BirdPhonics"] = "Bird Phonics";
    _localizationMapSwahili["BirdPhonics"] = "Sauti Ndege";
    _localizationMapMalay["BirdPhonics"] = "Fonik Burung";
    
    _localizationMapEnglish["FeedingTime"] = "Feeding Time";
    _localizationMapSwahili["FeedingTime"] = "Wakati wa kula";
    _localizationMapMalay["FeedingTime"] = "Masa Makan";
    
    _localizationMapEnglish["LineMatching"] = "Line Matching";
    _localizationMapSwahili["LineMatching"] = "Linganisha mistari";
    _localizationMapMalay["LineMatching"] = "Padanan Garisan";
    
    _localizationMapEnglish["MangoShop"] = "Mango Shop";
    _localizationMapSwahili["MangoShop"] = "Duka la embe";
    _localizationMapMalay["MangoShop"] = "Kedai Mangga";
    
    _localizationMapEnglish["MissingNumber"] = "Missing Number";
    _localizationMapSwahili["MissingNumber"] = "Namba iliyopotea";
    _localizationMapMalay["MissingNumber"] = "Nombor Hilang";
    
    _localizationMapEnglish["ReadingBird"] = "Reading Bird";
    _localizationMapSwahili["ReadingBird"] = "Ndege anayesoma";
    _localizationMapMalay["ReadingBird"] = "Burung Membaca";
    
    _localizationMapEnglish["WhatIsThis"] = "What is this?";
    _localizationMapSwahili["WhatIsThis"] = "Hii ni nini?";
    _localizationMapMalay["WhatIsThis"] = "Apakah ini?";
    
    _localizationMapEnglish["ThirtyPuzzle"] = "30 Puzzle";
    _localizationMapSwahili["ThirtyPuzzle"] = "Panga Namba";
    _localizationMapMalay["ThirtyPuzzle"] = "Teka-teki 30";
    
    _localizationMapEnglish["WordNote"] = "Word Note";
    _localizationMapSwahili["WordNote"] = "Tunga neno";
    _localizationMapMalay["WordNote"] = "Nota Perkataan";

    _localizationMapEnglish["QuickFacts"] = "Quick Facts";
    _localizationMapSwahili["QuickFacts"] = "Ukweli wa Uhakika";
    _localizationMapMalay["QuickFacts"] = "Fakta Pantas";

    _localizationMapEnglish["MultiplicationBoard"] = "Multiplication Lamp";
    _localizationMapSwahili["MultiplicationBoard"] = "Taa ya Kuzidishia";
    _localizationMapMalay["MultiplicationBoard"] = "Lampu Darab";

    _localizationMapEnglish["WordMatrix"] = "Word Matrix";
    _localizationMapSwahili["WordMatrix"] = "Chanzo cha Neno";
    _localizationMapMalay["WordMatrix"] = "Matriks Perkataan";

    _localizationMapEnglish["SentenceBridge"] = "Sentence Bridge";
    _localizationMapSwahili["SentenceBridge"] = "Daraja la Sentensi";
    _localizationMapMalay["SentenceBridge"] = "Jambatan Ayat";

    _localizationMapEnglish["WordWindow"] = "Word Window";
    _localizationMapSwahili["WordWindow"] = "Dirisha la Neno";
    _localizationMapMalay["WordWindow"] = "Tingkap Perkataan";

    _localizationMapEnglish["WordKicker"] = "Word Kicker";
    _localizationMapSwahili["WordKicker"] = "Mpigo wa Neno";
    _localizationMapMalay["WordKicker"] = "Penendang Perkataan";

    _localizationMapEnglish["MathKicker"] = "Math Kicker";
    _localizationMapSwahili["MathKicker"] = "Mpigo wa Hisabati";
    _localizationMapMalay["MathKicker"] = "Penendang Matematik";
    
    _localizationMapEnglish["PlaceValue"] = "Place Value";
    _localizationMapSwahili["PlaceValue"] = "Fungu la Thamani";
    _localizationMapMalay["PlaceValue"] = "Nilai Tempat";

    _localizationMapEnglish["Labeling"] = "Labeling";
    _localizationMapSwahili["Labeling"] = "Pachika Jina";
    _localizationMapMalay["Labeling"] = "Pelabelan";

    _localizationMapEnglish["LRComprehension"] = "Comprehension";
    _localizationMapSwahili["LRComprehension"] = "Ufahamu";
    _localizationMapMalay["LRComprehension"] = "Kefahaman";

    _localizationMapEnglish["BookwithQuiz"] = "Book with Quiz";
    _localizationMapSwahili["BookwithQuiz"] = "Kitabu Chenye Jaribio";
    _localizationMapMalay["BookwithQuiz"] = "Buku dengan Kuiz";

    _localizationMapEnglish["Do you want to take a test on this egg?"] = "Do you want to take a test on this egg?";
    _localizationMapSwahili["Do you want to take a test on this egg?"] = "Je, unataka kufanya jaribio kuhusu hili yai?";
    _localizationMapMalay["Do you want to take a test on this egg?"] = "Adakah anda mahu mengambil ujian untuk telur ini?";
    
    
    
    _localizationMapEnglish["Take the quiz to add me to your sea world!"] = "Take the quiz to add me to your sea world!";
    _localizationMapSwahili["Take the quiz to add me to your sea world!"] = "Fanya jaribio ili uniongeze kwenye\ndunia yako ya bahari!";
    _localizationMapMalay["Take the quiz to add me to your sea world!"] = "Ambil kuiz untuk menambah saya ke dunia laut anda!";

    _localizationMapEnglish["Congratulations!\nSee you at your sea world!"] = "Congratulations!\nSee you at your sea world!";
    _localizationMapSwahili["Congratulations!\nSee you at your sea world!"] = "Hongera!\nTuonane kwenye\ndunia yako ya bahari!";
    _localizationMapMalay["Congratulations!\nSee you at your sea world!"] = "Tahniah!\nJumpa lagi di dunia laut anda!";

    _localizationMapEnglish["Try again to add me to your sea world!"] = "Try again to add me to your sea world!";
    _localizationMapSwahili["Try again to add me to your sea world!"] = "Jaribu tena kuniongeza kwenye\ndunia yako ya bahari!";
    _localizationMapMalay["Try again to add me to your sea world!"] = "Cuba lagi untuk menambah saya ke dunia laut anda!";

    _localizationMapEnglish["Don't give up! Let's try it again!"] = "Don't give up! Let's try it again!";
    _localizationMapSwahili["Don't give up! Let's try it again!"] = "Usikate tamaa! Jaribu tena!";
    _localizationMapMalay["Don't give up! Let's try it again!"] = "Jangan berputus asa! Mari cuba lagi!";

    // NB(xenosoz, 2018): Migrated from ShapeMatching. Datasheet? I agree.
    vector<pair<string, string>> words_enUS_msMY = {
            // {"circle", "bulatan"},
            // {"square", "segi empat sama"},
            // {"triangle", "segi tiga"},
            // {"rectangle", "segi empat tepat"},
            // {"star", "bintang"},
            // {"rhombus", "rombus"},
            // {"diamond", "berlian"},
            // {"oval", "bujur"},
            // {"hexagon", "heksagon"},
            // {"pentagon", "pentagon"},
            // {"trapezoid", "trapezium"},
            // {"parallelogram", "segi empat selari"},
            // {"octagon", "oktagon"},
            // {"cone", "kon"},
            // {"sphere", "sfera"},
            // {"cylinder", "silinder"},
            // {"cube", "kiub"},
            // {"rectangular_prism", "prisma segi empat tepat"},
            // {"triangular_prism", "prisma segi tiga"},
            // {"pyramid", "piramid"},
            {"face", "permukaan"},
            {"faces", "permukaan"},
            {"side", "sisi"},
            {"sides", "sisi"},
            {"large", "besar"},
            {"medium", "sederhana"},
            {"small", "kecil"}
    };
    
    for (auto item : words_enUS_msMY) {
        string enUS = item.first;
        string swTZ = item.second;
        string msMY = item.second;
        
        auto key = enUS;
        _localizationMapEnglish[key] = enUS;
        _localizationMapMalay[key] = msMY;
        _localizationMapSwahili[key] = swTZ;
    }
    
}

std::vector<std::string> LanguageManager::getLocalizationMapKeys() {
    std::vector<std::string> rt;
    rt.clear();
    for (auto it: _localizationMapEnglish) {
        rt.push_back(it.first);
    }
    return rt;
}

bool LanguageManager::isSignLanguageMode()
{
    //return true;
    auto ret = UserDefault::getInstance()->getBoolForKey("sign_language_mode_on", false);
    return ret;
}
