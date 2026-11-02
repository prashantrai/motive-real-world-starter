# Motive Real-World Coding Starter (Java 16, STS4)

A portable **Maven / Spring Boot 2.7.18** project for a one-hour, screen-shared coding challenge.
Includes working REST controllers, an **H2 in-memory database**, Spring Data JPA, CSV upload/seed loading, JSON parsing, outbound HTTP calls to Nominatim and OSRM, and tests.

## 1. Requirements
- macOS Catalina / your existing STS4 is fine if it already works.
- **JDK 16.0.1** selected as the project JRE in STS4.
- Maven 3.5+ (STS includes Maven tooling; external `mvn` optional).
- Internet on first Maven import for dependency downloads.

## 2. Import into STS4
1. Unzip archive into a folder **outside** your STS workspace (e.g. `~/Projects/motive-real-world-starter`).
2. STS4: **File > Import > Maven > Existing Maven Projects**.
3. Select the unzipped `motive-real-world-starter` folder as the Root Directory, ensure `pom.xml` is detected, and Finish.
4. Right-click project > **Maven > Update Project** (Force Update if dependencies are missing).
5. **Project > Properties > Java Build Path**: check that JDK 16 is selected. If needed, add your installed JDK via Preferences > Java > Installed JREs.
6. Right-click `MotiveApplication.java` > **Run As > Spring Boot App** (or **Java Application**).
7. Right-click project > **Run As > Maven test** to verify tests.

From Terminal (if Maven installed): `mvn clean test` then `mvn spring-boot:run`.

## 3. Try endpoints (run app first)

```bash
curl 'http://localhost:8080/api/cities'
curl 'http://localhost:8080/api/cities?state=CA'
curl 'http://localhost:8080/api/cities/1'
curl 'http://localhost:8080/api/cities/top-states?k=2'
curl -F 'file=@src/main/resources/sample-cities.csv' 'http://localhost:8080/api/cities/import'
curl 'http://localhost:8080/api/cities/search-by-coordinates?lat=37.775&lon=-122.4183'
```


To fetch csv data and search for given longitude and latitude and return response: 

`curl 'http://localhost:8080/api/cities/search-by-coordinates?lat=37.775&lon=-122.4183'`

Expected response:

```bash
{
  "ID": "2645",
  "STATE_CODE": "CA",
  "STATE_NAME": "California",
  "CITY": "San Francisco",
  "COUNTY": "San Francisco",
  "LATITUDE": "37.775",
  "LONGITUDE": "-122.4183"
}
```

Address Lookup

`GET http://localhost:8080/api/geo/lookup?osmId=W50637691`

Sample Response: 

```bash
{
  "osm_type": "way",
  "osm_id": 50637691,
  "display_name": "Brandenburger Tor, Potsdam, Germany",
  "address": {
    "road": "Brandenburger Straße",
    "city": "Potsdam",
    "country": "Germany"
  }
}
```



To Save Entry in DB via POST Api: 

`POST http://localhost:8080/api/cities `

Sample:
 
```
Req:
{
  "city": "Sunnyvale",
  "stateCode": "CA",
  "latitude": 37.3688,
  "longitude": -122.0363
}

Resp: 
{
  "id": 4,
  "city": "Sunnyvale",
  "stateCode": "CA",
  "latitude": 37.3688,
  "longitude": -122.0363
}
```

Sample data has 3 cities and seeds **automatically on startup**; uploading the sample again adds 3 more records. There is no deduplication in this starter.

Optional **live Internet** requests (public third-party endpoints, subject to rate limits and service availability):

```bash
curl 'http://localhost:8080/api/geo/reverse?lat=37.7749&lon=-122.4194'
curl 'http://localhost:8080/api/geo/route?fromLat=37.7749&fromLon=-122.4194&toLat=37.3382&toLon=-121.8863'
```

The automated geocoding/routing tests use Mockito and **do not call external services**. The public Nominatim instance has usage limits; do not load-test it. Adjust identifying User-Agent if required by its policy.

## 4. Check H2 database
- Open `http://localhost:8080/h2-console`.
- JDBC URL: `jdbc:h2:mem:motivedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- Username: `sa`; password: *(blank)*.
- Run: `SELECT * FROM CITIES;`
- **H2 is ephemeral**: data is recreated after app restart.

### H2 Database Configuration/Property Details

| Property / Parameter | Type | Purpose | Description |
| :--- | :--- | :--- | :--- |
| **`spring.datasource.url`** | Spring Boot Key | Connection Target | Defines the JDBC driver string used to connect your application to the data source. |
| **`jdbc:h2:`** | JDBC Prefix | Driver Selector | Specifies that the application should use the H2 database engine driver. |
| **`mem:`** | H2 Mode | Storage Type | Configures the database to run purely in **RAM**. No data is saved to your local disk. |
| **`motivedb`** | Identifier | Database Name | Assigns a unique name (**`motivedb`**) to this specific in-memory database instance. |
| **`DB_CLOSE_DELAY=-1`** | H2 Parameter | Data Persistence | Prevents H2 from deleting the database when the last connection drops. `-1` keeps data alive until the JVM stops. |
| **`DB_CLOSE_ON_EXIT=FALSE`** | H2 Parameter | Lifecycle Control | Stops H2 from shutting down automatically on exit. It forces H2 to wait for **Spring Boot** to safely finish its own shutdown tasks first. |



## 5. Interview use
- The project uses intentionally simple sample `City` domain model. Replace it with the provided dataset/schema only after hearing the requirements.
- `controller/` = REST contracts; `service/` = business logic / CSV transforms; `repository/` = persistence; `model/` = JPA entities.
- If a task only needs transformations, use the service without persisting all input rows unnecessarily.
- Add tests before finalizing; use `CityApiTest` for endpoints and `GeoServiceTest` for mocked external calls.
- `top-states` currently uses an in-memory grouping for a small demo dataset; for large real datasets, prefer SQL aggregation plus database-side top-k.
- External HTTP services have timeouts, input validation, and basic error mapping; retries, authentication, pagination, and rate-limiting should be implemented if the challenge requires them.

## 6. Known limitations
- **Not a production setup.** Java 16 and Boot 2.7 are end-of-life. Use for local interview practice only.
- No security or authentication on sample API, no duplicate detection, and no production-grade exception/logging framework.
- CSV parsing accepts a US-Cities-style CSV header. Bad rows are skipped, but malformed CSV headers/file-level errors should be handled based on interviewer requirements.
- External live calls depend on Internet and third-party provider availability.

## 7. Troubleshooting
- Java compiler mismatch: make sure STS and Maven use Java 16 (not an older runtime). `pom.xml` uses `<maven.compiler.release>16</maven.compiler.release>`.
- Port already occupied: change `server.port` in `application.properties`.
- Cannot download dependencies: ensure Maven has network access, update Maven project, and re-run.
- H2 console empty: check URL spelling and make sure the application is running.
