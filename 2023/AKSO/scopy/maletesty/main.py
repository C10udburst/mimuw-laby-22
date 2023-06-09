import os
import filecmp

test_count = 11

def run(x):
    print("running {}".format(x))
    os.system("rm -f output{}".format(x))
    val = (os.system("../scopy.elf test{} output{}".format(x, x)) >> 8)
    if(val != 0):
        print("TEST {} return code {}".format(x, val))
    if(not filecmp.cmp("answer{}".format(x), "output{}".format(x))):
        print("WRONG ANSWER IN TEST {}".format(x))

for i in range(test_count):
    run(i)
