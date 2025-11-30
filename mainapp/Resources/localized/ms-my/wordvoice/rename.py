import sys
import glob
import os

for fn in glob.glob('*.wav'):
    os.rename(fn, fn.lower())
