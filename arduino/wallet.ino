#define D2 2
#define D3 3
#define D4 4
#define D5 5
#define D6 6
#define D7 7
#define D8 8
#define D9 9
#define D10 10
#define D11 11
#define D12 12

#define KEY_TRANS_TIME 100

#define NUM_KEY_MIN 1
#define NUM_KEY_MAX 7

#define KEY_ENTER 0

#define LED_G 6
#define LED_R 7

#include "uECC.h"
#include <string.h>

int key_to_pin [] = { 4,3,2,5, 10,9,11,8 };
char pin_to_key[] = { 0,0,
                      2,1,0,3,   // D2 ~ D5
                      0,0,
                      7,5,4,6 }; // D8 ~ D11

const char pwd[] = "1234567";

void ReadStartSignal();
bool InputPasswd();
void Flick(int pin_id, int repeat, int t);
void Flick(int pin_id, int repeat, int t1, int t2);

void setup()
{
  Serial.begin(9600);
  
  pinMode( LED_R, OUTPUT );
  pinMode( LED_G, OUTPUT );
  pinMode( LED_BUILTIN, OUTPUT );
  for(int i=NUM_KEY_MIN; i<=NUM_KEY_MAX; i++)
    pinMode( key_to_pin[i], INPUT );
}

void loop()
{
  ReadStartSignal();
  for(int i=0; i<2; i++)
  {
    digitalWrite( LED_BUILTIN, HIGH );
    delay(100);
    digitalWrite( LED_BUILTIN, LOW );
    delay(100);
  }
  
  while(true)
  {
    if( InputPasswd() )
    {
      Serial.write("ooo");
      digitalWrite(LED_G, HIGH);
      // Get Transaction Data
      uint8_t private_key[32], message_hash[32], signature[65];
      for (int i = 0; i < 32; i++)
      {
        while (Serial.available() <= 0)
          ;
        message_hash[i] = Serial.read();
      }
      memcpy(private_key, "\x2f\x65\x6a\x66\x54\x68\x5c\x19\x11\xc0\xed\x23\xba\xb2\xbd\x2a\x8b\x61\xf6\xd5\x92\x37\xc9\x5f\x74\xe8\x82\xac\x08\x41\xfd\x8d", 32);

      // Sign Transaction 
      int ret = rfc6979sha256p256sign(private_key, message_hash, signature + 1);
      signature[0] = (ret & 1) + 27;

      // Return Signed Data
      Serial.write(signature, 65);
      Flick( LED_G, 2, 250 );
    }
    else
    {
      digitalWrite(LED_R, HIGH);
      delay(1000);
      digitalWrite(LED_R, LOW);
    }
  }
}

void ReadStartSignal()
{
  int i=0;
  char s[] = "ooo";
  while(i<3)
  {
    while( Serial.available() <= 0 )
      ;
    int c = Serial.read();
    if(c == s[i])
      i++;
  }
  Serial.write("ok");
  return;
}


bool InputPasswd()
{
  int keyStat[16];
  char in_str[20];
  byte in_len = 0;
  
  keyStat[0] = 0;
  for(int i=NUM_KEY_MIN; i<=NUM_KEY_MAX; i++)
    keyStat[i] = 0;

  int match_len = 0;
  while(true)
  {
    for(int i=0; i<=NUM_KEY_MAX; i++)
    {
      int x = digitalRead(key_to_pin[i]);
      if( keyStat[i] != x )
      {
        keyStat[i] = x;
        if(x==1)  // Key Down
        {
          if(in_len==20)
          {
            in_len = 0;
            return false;
          }
          if( i==0 )  // Enter
          {
            in_str[in_len] = '\0';
            return strcmp(in_str,pwd)==0;
          }
          char c = '0'+i;
          Serial.write( &c, 1 );
          in_str[in_len++] = c;
          Flick( 13, KEY_TRANS_TIME/2, 1, 3 );
        }
        else  // Key Up
        {
          delay( KEY_TRANS_TIME );
        }
      }
    }
  }
}

void Flick(int pin_id, int repeat, int t)
{
  digitalWrite(pin_id, LOW);
  for(int i=0; i<repeat; i++)
  {
    delay(t);
    digitalWrite(pin_id, HIGH);
    delay(t);
    digitalWrite(pin_id, LOW);
  }
}

void Flick(int pin_id, int repeat, int t1, int t2)
{
  digitalWrite(pin_id, LOW);
  for(int i=0; i<repeat; i++)
  {
    delay(t2);
    digitalWrite(pin_id, HIGH);
    delay(t1);
    digitalWrite(pin_id, LOW);
  }
}

