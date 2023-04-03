import random
import sys
from datetime import datetime

wiersze = int(sys.argv[1])
kolumny = int(sys.argv[2])

ilosci = [0]*kolumny
while True:
    puste = [i for i in range(0, kolumny) if ilosci[i] < wiersze]
    random.seed(len(puste)+datetime.now().timestamp())
    if len(puste) <= 0:
        print(".")
        break
    x = random.choice(puste)
    print(chr(97 + x))
    ilosci[x] += 1

