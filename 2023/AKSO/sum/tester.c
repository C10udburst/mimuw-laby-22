#include <assert.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int abi_check(int64_t *x, size_t n);

const int64_t mod = 925361039;

int64_t po(int64_t a, int64_t b) {
    a %= mod;
    int64_t r = 1;
    while(b) {
        if(b & 1)
            r = r * a % mod;
        a = a * a % mod;
        b >>= 1;
    }
    return r;
}

int64_t eval1(int64_t *x, size_t n) {
    printf("ver1!\n");
    int64_t sum = 0;
    for(size_t i = 0; i < n; i++) {
        int64_t tmp = (x[i] % mod + mod) % mod;
        assert(tmp >= 0 && tmp < mod);
        int64_t shft = (((int64_t) i) * ((int64_t) i) * ((int64_t)64)) / n;
        int64_t mul = po((int64_t) 2, shft);
        assert(mul >= 0 && mul < mod);
        sum = (sum + tmp * mul) % mod;
    }
    return sum;
}

int64_t getumodval(int64_t x) {
    uint64_t y = x;
    int64_t ret = (int64_t) (y % mod);
    return ret;
}

int64_t eval2(int64_t *x, size_t n) {
    printf("ver2!\n");
    int64_t sum = 0;
    int neg = !!(x[n - 1] >> 63);
    for(size_t i = 0; i < n; i++) {
        int64_t tmp;
        if(!neg)
            tmp = getumodval(x[i]);
        else
            tmp = getumodval(~x[i]);
        assert(tmp >= 0 && tmp < mod);
        int64_t shft = (((int64_t) i) * ((int64_t)64));
        int64_t mul = po((int64_t) 2, shft);
        assert(mul >= 0 && mul < mod);
        sum = (sum + tmp * mul) % mod;
    }
    if(neg)
        sum = (mod + mod - (sum + 1)) % mod;
    return sum;
}


int read_test() {
    size_t n;
    assert(scanf("%lu", &n));
    
    int64_t *work_space = malloc(n * sizeof (int64_t));
    assert(work_space);
    
    for(size_t i = 0; i < n; i++)
        assert(scanf("%lx", &work_space[i]));

    int64_t valmoda = eval1(work_space, n);
        
    if(!abi_check(work_space, n)) {
        printf("ABI FAIL\n");
        return 0;
    }

    for (size_t i = 0; i < n; ++i) {
        int64_t tmp;
        assert(scanf("%lx", &tmp));
        if (work_space[i] != tmp) {
            printf("Pod indeksem %zu\n"
                "jest        %016" PRIx64 ",\n"
                "powinno być %016" PRIx64 ".\n", i, work_space[i], tmp);
            return 0;
        }
    }

    int64_t valmodb = eval2(work_space, n);

    if(valmoda != valmodb) {
        printf("BROKEN TESTCASE???\n");
        printf("0x%016" PRIx64 "\n", valmoda);
        printf("0x%016" PRIx64 "\n", valmodb);
        return 0;
    }
    
    free(work_space);
    return 1;
}

int normalne_testy(size_t t) {
    for(size_t i = 0; i < t; i++) {
        printf("------\nTEST %lu\n", i);
        if(read_test()) {
            printf("OK\n");
        }
        else {
            printf("FAIL\n");
            return 1;
        }
    }
    return 0;
}

int tescior_gigancior() {

    size_t n = (((size_t)1) << 29) - 1;
    
    int64_t *work_space = malloc(n * sizeof (int64_t));

    int64_t val;

    assert(scanf("%lx", &val));

    for(size_t i = 0; i < n; i++) {
        work_space[i] = val;
        val ^= val << 13;
        val *= 1064886312845077141;
        val ^= val >> 7;
    }

    int64_t valmoda = eval1(work_space, n);

    if(!abi_check(work_space, n)) {
        printf("ABI FAIL\n");
        return 1;
    } 

    int64_t valmodb = eval2(work_space, n);

    int64_t hash = 0;
    
    for(size_t i = 0; i < n; i++) {
        hash ^= work_space[i];
        hash += 0x1234567890abcdef;
        hash ^= hash << 13;
        hash *= 931066282152586997;
    }
    
    if(valmoda != valmodb) {
        printf("MODULAR MISMATCH\n");
        printf("0x%016" PRIx64 "\n", valmoda);
        printf("0x%016" PRIx64 "\n", valmodb);
        return 1;
    }

    int64_t hash2;

    assert(scanf("%lx", &hash2));


    if(hash != hash2) {
        printf("FAIL!\n");
        return 1;
    }


    printf("OK!\n");
    return 0;
    
}

int main() {
    size_t t;
    assert(scanf("%lu", &t));
    if(t != 0)
        return normalne_testy(t);
    else
        return tescior_gigancior();
}
