#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-observe.h"
#include "os/dev/leds.h"
#include "sys/etimer.h"

#include "sys/log.h"
#define LOG_MODULE "light sensor"
#define LOG_LEVEL LOG_LEVEL_DBG

#define PRESENCE_THRESHOLD 50

static int counter = 0;
extern bool lightOn; 
bool presence = false;
bool mode = true;
unsigned long timestamp;



static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);


EVENT_RESOURCE(res_presence,
	"title=\"Presence sensor\";methods=\"GET/PUT mode=<value>\";rt=\"int\";obs",
	res_get_handler,
	NULL,
    res_put_handler,
	NULL,
	res_event_handler);


static void res_event_handler(void){
	counter++;
	coap_notify_observers(&res_presence);
}



static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
  
  const char *modeTxt = NULL;
  char msg[8];
  int length = sizeof(msg);		
	
  if(coap_get_query_variable(request, "mode", &modeTxt)) {
    if (mode){
		memcpy(buffer, "AUTO", length);
		
		
	}
	else {
		memcpy(buffer, "MANUAL", length);
		
	}

 	coap_set_header_content_format(response, TEXT_PLAIN); /* text/plain is the default, hence this option could be omitted. */
  	coap_set_header_etag(response, (uint8_t *)&length, 1);
  	coap_set_payload(response, buffer, length);
  } else {
	
	timestamp = (unsigned long)time(NULL);

	if(request != NULL){
		LOG_DBG("Observing handler number %d\n", counter); 
	}

	unsigned int accept = -1;

	coap_get_header_accept(request, &accept);
	
	if (accept== -1)
		accept = APPLICATION_JSON;

	if(accept == APPLICATION_JSON) {
		coap_set_header_content_format(response, APPLICATION_JSON);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"p\":%d,\"dt\":%lu,\"m\":%d}", presence, timestamp, mode);
		coap_set_payload(response, buffer, strlen((char *)buffer));
	}
	else {
		coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
		const char *msg = "Supporting content-type application/json";
		coap_set_payload(response, msg, strlen(msg));
  	}
  }
 
}



static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  size_t len = 0;
  const char *modeTxt = NULL;
  int success = 1;

  if(success && (len = coap_get_post_variable(request, "mode", &modeTxt))) {
    LOG_DBG("mode %s\n", modeTxt);

    if(strncmp(modeTxt, "AUTO", len) == 0) {
      mode=true;
	  leds_off(LEDS_NUM_TO_MASK(LEDS_YELLOW));
    } else if(strncmp(modeTxt, "MANUAL", len) == 0) {
      mode=false;
	  leds_on(LEDS_NUM_TO_MASK(LEDS_YELLOW));
    } else {
      success = 0;
    }
  } else {
    success = 0;
  } if(!success) {
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
}