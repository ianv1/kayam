import requests, base64
def gen(name,prompt,extra_neg=""):
    neg=("hijab, headscarf, veil, muslim dress, east asian, chinese, japanese, korean, "
         "text, words, letters, caption, map labels, watermark, signature, "
         "blurry, deformed, extra limbs, mismatched body, disconnected head, bad anatomy, "+extra_neg)
    r=requests.post("http://127.0.0.1:7860/sdapi/v1/txt2img",json={
      "prompt":prompt,"negative_prompt":neg,"width":1024,"height":717,
      "steps":32,"cfg_scale":8,"sampler_name":"Euler a","seed":-1},timeout=300)
    r.raise_for_status()
    open(f"/tmp/{name}.png","wb").write(base64.b64decode(r.json()["images"][0].split(",",1)[-1]))
    print("done",name,flush=True)

STYLE=(", detailed soft watercolor children's storybook illustration, painterly, "
       "warm colors, plain white background")
KAVITA=("Kavita, a cheerful young Indian girl with long dark hair in a braid, "
        "wearing a red and gold traditional Indian kurta dress")
RAVI=("Ravi, a young Indian boy with short black hair, brown skin, "
      "wearing a light blue collared shirt and shorts")
BOYNEG="Ravi as a girl, two girls, female Ravi, boy wearing a dress, boy in a skirt, long hair on boy"

gen("q414_0", f"{KAVITA} and {RAVI} standing together smiling as friends in front of a cozy house"+STYLE, BOYNEG)
gen("q414_1", f"{KAVITA} sitting at a wooden table happily reading a letter, an open book on the table"+STYLE)
gen("q414_2", f"{KAVITA} holding a letter and looking at a simple hand drawn paper map, curious"+STYLE)
gen("q414_3", f"{KAVITA} walking along a garden path, turning right beside a big leafy tree"+STYLE)
gen("q414_4", f"full body of {KAVITA} walking past a yellow wooden picket fence, correct body proportions, head body and legs connected naturally"+STYLE)
gen("q414_5", f"{KAVITA} standing in front of a small house that has a bright blue front door"+STYLE)
gen("q414_6", f"{KAVITA} knocking on the bright blue wooden door of a house"+STYLE)
gen("q414_7", f"{RAVI} standing in his open doorway warmly welcoming {KAVITA} who stands outside, both happy"+STYLE, BOYNEG)
gen("q414_8", f"{KAVITA} the girl and {RAVI} the boy happily greeting each other at the doorway, joyful friends"+STYLE, BOYNEG)
print("ALL DONE")
