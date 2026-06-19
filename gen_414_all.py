import requests, base64
KAV=base64.b64encode(open("/tmp/ref_kavita.png","rb").read()).decode()
RAV=base64.b64encode(open("/tmp/ref_ravi.png","rb").read()).decode()
CTRL="IP Adapter Plus (SDXL Base)"
def c(img,w):
    return {"control_name":CTRL,"image":img,"weight":w,"guidanceStart":0.0,"guidanceEnd":1.0,
            "noPrompt":False,"globalAveragePooling":False,"downSamplingRate":1.0,
            "controlImportance":"balanced","inputOverride":"","targetBlocks":[]}
NEG=("hijab, headscarf, east asian, chinese, text, words, letters, watermark, signature, "
     "blurry, deformed, extra limbs, bad anatomy, mismatched body, blended faces, same person twice, boy in a dress")
STYLE=", soft watercolor children's storybook illustration, painterly, warm colors, plain white background"
def gen(name,scene,controls,seed=7):
    body={"prompt":scene+STYLE,"negative_prompt":NEG,"width":1024,"height":717,
          "steps":30,"cfg_scale":7,"seed":seed,"controls":controls}
    r=requests.post("http://127.0.0.1:7860/sdapi/v1/txt2img",json=body,timeout=240); r.raise_for_status()
    open(f"/tmp/f414_{name}.png","wb").write(base64.b64decode(r.json()["images"][0].split(",",1)[-1]))
    print("done",name,flush=True)

K=[c(KAV,0.8)]
BOTH=[c(KAV,0.55),c(RAV,0.55)]
gen("0","Kavita the Indian girl and Ravi the Indian boy standing together smiling as two happy friends in front of a cozy house",BOTH,5)
gen("1","Kavita the Indian girl sitting at a wooden table happily reading a letter, an open book on the table",K)
gen("2","Kavita the Indian girl holding a letter and looking at a simple hand drawn paper map, curious",K)
gen("3","Kavita the Indian girl walking along a garden path turning right beside a big leafy tree",K)
gen("4","full body of Kavita the Indian girl walking past a yellow wooden picket fence, natural proportions",K)
gen("5","Kavita the Indian girl standing in front of a small house with a bright blue front door",K)
gen("6","Kavita the Indian girl knocking on the bright blue wooden door of a house",K)
gen("7","Ravi the Indian boy standing in his open doorway on the right warmly welcoming Kavita the Indian girl standing outside on the left, both happy",BOTH,3)
gen("8","Kavita the Indian girl and Ravi the Indian boy happily waving and smiling at each other at the doorway of a house, joyful friends",BOTH,9)
print("ALL DONE")
