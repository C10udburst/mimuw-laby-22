#include <stdint.h>

void sum(int64_t* x, uint32_t n) {
    for(uint32_t i = 0; i < n; ++i) {
        uint64_t mj = i;
        mj *= i;
        mj *= 64;
        for(uint64_t j = 0; j < mj; j++) {
            x[i] = x[i]<<1;
            for(uint64_t ii = i+1; i < n; i++) {
                x[ii]++;
                asm("jnc end_shl");
            }
            asm("end_shl:");
        }
    }
}