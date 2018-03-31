#include <Arduino.h>

#include <Crypto.h>
#include <RNG.h>
#include <TransistorNoiseSource.h>

#include <Keypad.h>

#include "encryption.h"
#include "myKeypad.h"
#include "lcd.h"


void readPassword(uint8_t key[]);

void PRNGSetup() {
  TransistorNoiseSource noise(A1);
  RNG.begin("Etherarduino 0.1");
  RNG.addNoiseSource(noise);
}

void PRNGLoop() {
  RNG.loop();
}

void generateKey(uint8_t prikey_temp[32], uint8_t prikey_cipher[32]) {
  Serial.println("Generating New Private Key...");
  lcdprint("Gen key...");

  // Randomly Generate Key
  while (!RNG.available(32)) {
    RNG.loop();
  }
  RNG.rand(prikey_temp, 32);
  RNG.save();

  // AES Encryption
  char password[64] = {};
  uint8_t key[32];
  Serial.println("waiting for password");
  lcdprint("password:");
  keypadRead(password, 64);
  for (int i = 0; i < 64; ++i) {
    if (isDigit(password[i])) {
      password[i] = password[i] - '0';
    } else {
      password[i] = password[i] - 'A';
    }
  }
  for (int i = 0; i < 32; ++i) {
    key[i] = password[i * 2] * 16 + password[i * 2 + 1];
  }
  encryptPrivKey(prikey_temp, prikey_cipher, key);

  clean(prikey_temp, 32);
  clean(password);
  clean(key);

  delay(500);
  Serial.println("done");
  lcdprint("done");
}
