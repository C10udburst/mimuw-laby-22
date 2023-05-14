#!/usr/bin/env python3
import ctypes
import os
import sys

INT_MIN = -9223372036854775808
INT_MAX = 9223372036854775807
INT_MASK = 0xFFFFFFFFFFFFFFFF
SMASK = 1<<63
libsum = ctypes.CDLL(os.path.join(os.getcwd(), 'libsum.so'))

def twos_comp(val, bits):
    """compute the 2's complement of int value val"""
    if (val & (1 << (bits - 1))) != 0: # if sign bit is set e.g., 8bit: 128-255
        val = val - (1 << bits)        # compute negative value
    return val    

def generate_x():
    x = []
    imp = input(">")
    if not imp.startswith("["):
        imp = f"[{imp}]"
    x = [twos_comp(i & INT_MASK, 64) for i in eval(imp)]
    return x
            

def calculate_y(x, n):
    mod = 1<<(64*n)
    y = 0
    for i, xi in enumerate(x):
        y += xi * (2 ** int(64 * (i ** 2) / len(x)))
        y = y % mod
    return y

def chunks(xs, n):
    n = max(1, n)
    return (xs[i:i+n] for i in range(0, len(xs), n))

def n_ff(n):
    return int("ff"*n, 16)

def make_yarray(y, n):
    y_array = []
    yhex = hex(y & n_ff(len(hex(y))-2))[2:]
    chars = 64*2//8
    while len(yhex)//chars < n:
        yhex = ('0' if y >= 0 else 'f') + yhex
    for chunk in chunks(yhex, chars):
        y_array.append(int(chunk, 16))
    return y_array[::-1]
        

def convert_to_carray(x):
    return (ctypes.c_int64 * len(x))(*x)

def pprint(arr, n):
    for i in range(n):
        e = arr[i] & INT_MASK
        print(f"{e:016x}", end=' ')
    print()

x = generate_x()
n = len(x)
x_array = convert_to_carray(x)
y = calculate_y(x,n)
y_array = make_yarray(y, n)

libsum.sum(ctypes.byref(x_array), n)

errored = False
for i in range(len(x)):
    found = x_array[i] & INT_MASK
    expected = y_array[i] & INT_MASK
    if found != expected:
        print(f"[{i}]")
        print(f"  found:    {found:016x}")
        print(f"  expected: {expected:016x}")
        errored = True

print(f"{n=}")
if n < 300:
    print("x=", end='')
    pprint(x, n)
    print("y=", end='')
    pprint(y_array, n)
    print("r=", end='')
    pprint(x_array, n)

if not errored:
    print("\033[92msuccess!\033[0m")
sys.exit(1 if errored else 0)