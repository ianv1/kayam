import requests, base64
def gen(name,prompt):
    neg=("hijab, headscarf, muslim veil, turban, hair covering, "
         "text, words, letters, caption, watermark, signature, "
         "blurry, deformed, extra limbs, ugly, scary, dark, photo realistic")
    r=requests.post("http://127.0.0.1:7860/sdapi/v1/txt2img",json={
      "prompt":prompt,"negative_prompt":neg,"width":1024,"height":776,
      "steps":32,"cfg_scale":8,"sampler_name":"Euler a","seed":-1},timeout=300)
    r.raise_for_status()
    open(f"/tmp/{name}.png","wb").write(base64.b64decode(r.json()["images"][0].split(",",1)[-1]))
    print("done",name,flush=True)

STYLE=(", soft watercolor cartoon children's storybook illustration, "
       "warm gentle colors, hand drawn, clean plain white background, full color")
# consistent characters
MOM="a kind Chinese mother with black hair tied in a neat bun, wearing a soft pink blouse"
BOY="a young Chinese boy with short black hair wearing a blue t-shirt"
GIRL="a young Chinese girl with black hair in pigtails wearing a yellow dress"

gen("p0", f"{MOM} sitting on a cozy sofa with her two children, {BOY} and {GIRL}, happy warm family"+STYLE)
gen("p1", f"{MOM} lying sick and tired on a sofa, pale and unwell, holding a thermometer, resting under a blanket"+STYLE)
gen("p2", f"{MOM} sweeping the kitchen floor with a broom, busy doing all the housework, tired"+STYLE)
gen("p3", f"{BOY} and {GIRL}, both wearing school backpacks, standing together ready to go to school, cheerful"+STYLE)
gen("p4", f"{BOY} smiling with a bright glowing yellow lightbulb above his head, having a good idea"+STYLE)
gen("p5", f"{BOY} happily mopping and cleaning the kitchen floor, doing the housework, cheerful"+STYLE)
gen("p6", f"{MOM} warmly hugging her happy child, loving moment, mother feeling better and smiling"+STYLE)
print("ALL DONE")
