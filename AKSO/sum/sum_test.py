import ctypes
import os
import random
import sys

"""
void sum(int64_t *x, size_t n);
The arguments of this function are a pointer x to a non-empty array of 64-bit integers in a two's complement representation and the size n of this array. The function's operation is defined by the following pseudocode:

y = 0;
for (i = 0; i < n; ++i)
  y += x[i] * (2 ** floor(64 * i * i / n));
x[0, ..., n-1] = y;
where ** denotes a power-up, and y is a (64 * n)-bit number in the complement representation to two. The function should place the result in the x array in little-endian order. The function should perform the calculation "in-place," using only the memory pointed to by x, and should not use additional memory.

It is free to assume that the pointer to x is valid and that n has a positive value less than 2 to the power of 29.
"""

seed = None
if len(sys.argv) < 2:
    seed = input("seed> ")
    if seed == "":
        seed = str(os.getpid())
else:
    seed = sys.argv[1]
        
random.seed(seed)

INT_MIN = -9223372036854775808
INT_MAX = 9223372036854775807
INT_MASK = 0xFFFFFFFFFFFFFFFF
libsum = ctypes.CDLL(os.path.join(os.getcwd(), 'libsum.so'))

def generate_x():
    x = []
    for _ in range(random.randint(1, 2048)):
        x.append(random.randint(INT_MIN, INT_MAX))
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

print(f"{n=}, {seed=}")
print("x=", end='')
pprint(x, n)
print("y=", end='')
pprint(y_array, n)
print("r=", end='')
pprint(x_array, n)

if not errored:
    print("\033[92msuccess!")