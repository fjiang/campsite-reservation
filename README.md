# Campsite-reservation-service
Restful API service to manage the island campsite reservations.

3 endpoints are exposed for availability check, campsite reservation and reservation change.

Restriction and rules:

The campsite will be free for all.

The campsite can be reserved for max 3 days.

The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance. Reservations can be cancelled anytime.


## REST API
The REST API endpoints are described below.

Refer to swagger for more detailed API doc

### Get dates availability

#### Request

`Get /campsite/availability`

`http://localhost:8080/campsite/availability?startDate=2021-12-10&endDate=2021-12-12`

#### Response
Status: 200 OK

{"availableDates":["2021-12-10","2021-12-11","2021-12-12"]}


### Reserve Campsite

#### Request

`Post /campsite/reserve`

`http://localhost:8080/campsite/reserve`

#### Response
Status: 200 OK

{
    "processingStatus": "SUCCEEDED",
    "trackId": "6fdae281c1cc4e2099896aeddb75c72c"
}

### Change Campsite Reservation

#### Request

`Post /campsite/change`

`http://localhost:8080/campsite/change`

#####Body:

`{
     "bookingId": "ec89acab88c14be98768e804bbbf2f53",
     "changeReserveOperation": "CHANGE",
     "startDate": "2021-12-09",
     "endDate": "2021-12-11"
 }`

#### Response
Status: 200 OK

##### Cancel:
{
    "processingStatus": "SUCCEEDED"
}

##### Change:

{
    "processingStatus": "SUCCEEDED",
    "trackId": "6fdae281c1cc4e2099896aeddb75c72c"
}
