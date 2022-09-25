#include "contiki.h"
#include "coap-engine.h"
#include <string.h>
#include "os/dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "light sensor"
#define LOG_LEVEL LOG_LEVEL_DBG

bool lightValue = false;

static void res_get_handler(coap_message_t *packet, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_post_put_handler(coap_message_t *packet, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

/*
"title=\"Light actuator\"; methods=\"GET/POST/PUT\";obs;rt=\"light\""
"title=\"Light, POST/PUT state=on|off\";obs;rt=\"light\""	
*/

RESOURCE (
    res_light,
    "title=\"Light actuator\"; methods=\"GET/POST/PUT state=ON|OFF\";rt=\"light\";obs",
    res_get_handler,
    res_post_put_handler,
    res_post_put_handler,
    NULL);

static void res_get_handler(coap_message_t *packet, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
    unsigned int accept = -1;
    coap_get_header_accept(packet, &accept);
	
	if(accept == -1)
		accept = APPLICATION_JSON;

    if(accept == APPLICATION_JSON){
        coap_set_header_content_format(response, APPLICATION_JSON);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"state\":%d}", lightValue);
		coap_set_payload(response, buffer, strlen((char *)buffer));
    }
	 else{
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
		const char *msg = "Supporting content-type application/json";
		coap_set_payload(response, msg, strlen(msg));
    }


}

static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	if(request != NULL) {
		LOG_DBG("POST/PUT Request Sent\n");
	}

	size_t len = 0;
	const char *state = NULL;
	int check = 1;

	if((len = coap_get_post_variable(request, "state", &state))) {
		LOG_DBG("state: \n");
		
		
		if (!strcmp(state,"ON")){
			LOG_DBG("state: on \n");
			leds_on(LEDS_NUM_TO_MASK(LEDS_GREEN));
			leds_off(LEDS_NUM_TO_MASK(LEDS_RED));
            lightValue=true;
		}
		
		else if(!strcmp(state, "OFF")){
			LOG_DBG("state: off \n");
			leds_on(LEDS_NUM_TO_MASK(LEDS_RED));
			leds_off(LEDS_NUM_TO_MASK(LEDS_GREEN));
            lightValue =false;

		}
		else{
			check = 0;
		}	
	}
	else{
		check = 0;
	}

	if (check){
		coap_set_status_code(response, CHANGED_2_04);
	}
	else{
		coap_set_status_code(response, BAD_REQUEST_4_00);
	}
		
}


