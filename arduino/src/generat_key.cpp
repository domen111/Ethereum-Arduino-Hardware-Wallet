#include <Arduino.h>

#include <Crypto.h>
#include <RNG.h>
#include <TransistorNoiseSource.h>

void PRNGSetup() {
  TransistorNoiseSource noise(A1);
  RNG.begin("Etherarduino 0.1");
  RNG.addNoiseSource(noise);
}

void generateKey(uint8_t prikey[32]) {
  Serial.println("Generating New Private Key...");
  while (!RNG.available(32)) {
    RNG.loop();
  }
  RNG.rand(prikey, 32);
  RNG.save();
  delay(500);
  Serial.println("done");
}

void PRNGLoop() {
  RNG.loop();
}
