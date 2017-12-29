#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <ctype.h>

// Enable CBC mode.
#define CBC 1
#define CTR 0
#define ECB 0

#include "tiny-AES-c/aes.h"

uint8_t hex2int(char a, char b)
{
    uint8_t res = 0;
    if (isalpha(a)) res += a - 'a' + 10;
    else res += a - '0';
    res *= 16;
    if (isalpha(b)) res += b - 'a' + 10;
    else res += b - '0';
    return res;
}

int main(void)
{
    char hex_privkey[] = "2f656a6654685c1911c0ed23bab2bd2a8b61f6d59237c95f74e882ac0841fd8d";

    uint8_t key[16] = { '1', '2', '3'};
    uint8_t out[] = { 0x52, 0x7d, 0x27, 0x86, 0xf8, 0x43, 0xaf, 0x73, 0x3d, 0x16, 0xb0, 0xf2, 0x94, 0x24, 0x25, 0xd3, 
                      0x19, 0xae, 0x87, 0x7b, 0x0d, 0x4d, 0x31, 0xe0, 0x0a, 0x20, 0xbd, 0x37, 0xa6, 0x48, 0x23, 0x67, 
                      0x30, 0x1d, 0x2d, 0x9f, 0xd2, 0xec, 0xdc, 0x1f, 0x65, 0xda, 0x70, 0x9f, 0x14, 0x69, 0xe1, 0xe5, 
                      0x2d, 0xd8, 0x6b, 0xf4, 0x0b, 0x64, 0x23, 0x23, 0xd5, 0xff, 0xd4, 0xcd, 0x95, 0x6f, 0x92, 0x7c };
    uint8_t iv[]  = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
    uint8_t in[64];
    struct AES_ctx ctx;

    for (int i = 0; hex_privkey[i]; i+=2) {
        in[i / 2] = hex2int(hex_privkey[i], hex_privkey[i + 1]);
    }

    AES_init_ctx_iv(&ctx, key, iv);
    AES_CBC_encrypt_buffer(&ctx, in, sizeof(in));

    printf("CBC encrypt: ");

    if (0 == memcmp((char*) out, (char*) in, 64))
    {
        printf("SUCCESS!\n");
    }
    else
    {
        printf("FAILURE!\n");
    }

    for (int i = 0; i < 64; i++) {
        printf("%02x", in[i]);
    }
    puts("");

    return 0;
}
