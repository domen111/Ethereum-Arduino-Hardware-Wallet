#include <Arduino.h>
#include <AES.h> 

#define PKEY_LEN 32
 
void encryptPrivKey(uint8_t plain[PKEY_LEN], uint8_t cipher[PKEY_LEN], uint8_t key[32]) {
  AES256 aes256;
  aes256.setKey(key, PKEY_LEN);
  for (int i = 0; i < PKEY_LEN; i += 16) {
    aes256.encryptBlock(cipher + i, plain + i);
  }
}
 
void decryptPrivKey(uint8_t plain[PKEY_LEN], uint8_t cipher[PKEY_LEN], uint8_t key[32]) {
  AES256 aes256;
  aes256.setKey(key, 32);
  for (int i = 0; i < PKEY_LEN; i += 16) {
    aes256.decryptBlock(plain + i, cipher + i);
  }
}
