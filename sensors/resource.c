#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-log.h"
#include "sys/log.h"
#include "coap-blocking-api.h"
#include "sys/etimer.h"
#include "os/dev/leds.h"

/* Log configuration */

#include "sys/log.h"
#define LOG_MODULE "Node"
#define LOG_LEVEL LOG_LEVEL_DBG

#define THRESHOLD 60


extern coap_resource_t res_light;
extern coap_resource_t res_presence;


extern bool lightValue;
extern bool mode;
extern bool presence;

bool registered = false;
bool oldLightValue=false;

#define SERVER_EP ("coap://[fd00::1]:5683")

char *service_registration = "registrant";

extern unsigned short node_id;



PROCESS(node, "Node");
AUTOSTART_PROCESSES(&node);

void client_chunk_handler(coap_message_t *response){
	
    const uint8_t *chunk;

    if (response == NULL){
		return;
	}
    
	int len = coap_get_payload(response, &chunk);

	LOG_INFO("|%.*s \n", len, (char *)chunk);
    
    
}

void init_led(){
    leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
    return;
}

int isPresence(){
    if ((rand()%100+1) > THRESHOLD)
        return true ;
    else
        return false;
}

void turnON(){
    leds_on(LEDS_NUM_TO_MASK(LEDS_GREEN));
    leds_off(LEDS_NUM_TO_MASK(LEDS_RED));
    LOG_INFO("Light ON\n");
}

void turnOFF(){
    leds_on(LEDS_NUM_TO_MASK(LEDS_RED));
    leds_off(LEDS_NUM_TO_MASK(LEDS_GREEN));
    LOG_INFO("Light OFF\n");
}


void change_light(bool lightValue){
    leds_toggle(LEDS_NUM_TO_MASK(LEDS_GREEN) && LEDS_NUM_TO_MASK(LEDS_RED) );  
}

static struct etimer e_timer;

PROCESS_THREAD(node, ev, data){
    static coap_endpoint_t server;
    static coap_message_t request[1];
    

    PROCESS_BEGIN();

    coap_activate_resource(&res_light, "actuator/lightBulb");
    coap_activate_resource(&res_presence, "sensor/presenceDetector");


    coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server);

    coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
    coap_set_header_uri_path(request, service_registration);

    COAP_BLOCKING_REQUEST(&server, request,client_chunk_handler);

    LOG_INFO("registered\n");

  
    etimer_set(&e_timer,5*CLOCK_SECOND);
    init_led();

    turnOFF();
    lightValue =false;
    mode=true;


    while(1){
        PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&e_timer));
        oldLightValue = lightValue;

        if (mode){
            presence = isPresence();
            if (presence ){
                LOG_DBG("Presence detected!\n");
                lightValue = true;
                if (oldLightValue != lightValue){
                    turnON();
                    res_presence.trigger();
                }      
            }
            else 
            {   
                LOG_DBG("Presence not detected!\n");
                lightValue=false;
                if (oldLightValue != lightValue){
                    turnOFF();
                    res_presence.trigger();
                }
            }
    }
            
        etimer_reset(&e_timer);
    }

    PROCESS_END();

}
