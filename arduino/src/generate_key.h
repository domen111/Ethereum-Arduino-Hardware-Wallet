#include <Arduino.h>

void PRNGSetup();
void PRNGLoop();
void generateKey(uint8_t prikey_temp[32], uint8_t prikey_cipher[32]);
