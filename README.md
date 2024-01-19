# CS 122B Project 5 Submission

## Project 5:
- # General
    - #### Team#: spin-mop
    
    - #### Names: Megan Tsoi and Sandra Wang 
    
    - #### Project 5 Video Demo Link: (Our Results Are Not Capturable) https://youtu.be/AzSKsJ97ka0

    - #### Instruction of deployment:
    - No Special Instructions

    - #### Collaborations and Work Distribution:
    - Both Worked and got Sick
    - covid and gastroentritis 


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    - WebContent/META-INF/context.xml
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    - We added the conection pooling resource in the context.xml under WEBCONTENT/META-INF. Every time a servlet requires a database connection, it will look in the database                 connection pool for idle connections and then reuse them instead of creating new ones.
      For example, when we are done searching for a specific movie on search bar that connection is returned to the connection pool instead of being closed and set to idle. Then when        we need another jdbc connection, we can look into the pool and perhaps use the connection we just returned to the pool earlier. 
    
    - #### Explain how Connection Pooling works with two backend SQL.
    - There are two Connection pool data resources, one for the master and the other for local connection. For servlets that need require writing or modifying the database, it will
      look in the connection pool for connection to the master instance, because that is where write requests can be sent. For any reading requests it will look into the local one 
      instead (which could be the slave instance or master instance). 
      

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
    - WebContent/META-INF/context.xml

    - #### How read/write requests were routed to Master/Slave SQL?
    - Similar to my answer for Connection pooling, we modified the servlets that required writing requests (INSERT, DELETE, UPDATE) to establish a connection to the master data             source, while servlets that required only read requests established a connection with localHost which was either the master or slave. The load balancer is then set up to redirect      it's http requests to the master/slave instances. We opened up port 3306 of each master/slave instance to each other as well as to the load balancer so they could communicate. 
    

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
    - Go into logScript directory. Place in created csv file from Tomcat folder into the logScript directory. Run log_processing.py when ready and will print out TS and TJ.
    - 


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTPS/10 threads                       | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 4: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ?? 

Domain name Website alternative (EXTRA CREDIT):
https://fab-flix.crabdance.com:8443/2023-fall-cs122b-spin-mop/index.html
## Video Demonstrations:
- Project 1 --> https://youtu.be/dRmajeKBb8Q
- Project 2 --> https://youtu.be/raARUqySSEg
- Project 3 --> https://youtu.be/eQSIXNcqm5c
- Project 4 --> https://youtu.be/kjwoFmkknE0?si=LZy2g6ErKLU7G54h

## Team Member Contributions (WIP):
- Megan Tsoi
    - Created the SingleMovieServlet.java, single-movie.js and single-movie.html
    - Modified the SingleStarServelet.java, single-star.js and single-star.html
    - Made unstylictic style decisions for website 🌸  
- Sandra Wang
    - Helped with configuring servers and setting up project
    - Created the MovieListServlet.java, dashboard-index.js and index.html
    - Handled the AWS aspect of the project and filmed the demo video

## Notes: 
- Normal Running Instructions
- To run parser, is set aside in special directory **xmlparser**, which comes with its own pom.xml
- The logo will lead you to the Main page

## Project 4 Disclaimer:
### Autocomplete
- Autocomplete results are only triggered with meaningul queries. In otherwords, words such as "the" and "an" will not trigger matching. This is because mysql match...against ignores stopwords, so in order for the matching to work properly on mysql we also got rid of the stopwords from our tokenized query.

## Project 3 Information:
### Prepared Statements:
- MovieListServlet:
    - Line 119: before creating PreparedStatement, will build query with appropriate parameters depending on what is searched/clicked
        - all information concatenated to query String is guaranteed to not be from users (data not from user inputs)
        - PreparedStatement used for all search parameters (user input)
    - Line 168/194: uses movie_id as query parameter which is not from user input but is made safe anyways.
- SingleMovieServlet:
    - Line 57/78/94/111: PreparedStatement used for movieId and starId (from MySQL database) comparisons
- SingleStarServlet:
    - Line 57: PreparedStatement used for starId (from MySQL database)
- AddStarServlet:
    - Line 43: calls function on Line 84, which uses id, star_name, and birthYear as parameters (user input)
    - Line 68: uses PreparedStatement for efficiency
    - Line 104: PreparedStatement used for starId (user input)
- SearchServlet: (possibly for Project 4 for autocomplete)
    - Line 59: PreparedStatement used for LIKE parameter movie.title (user input)
- LoginServlet:
    - Line 44: PreparedStatement used for email finding (user input)
- DashboardLoginServlet
    - Line 44: PreparedStatement used for email finding (user input)
- ShoppingCartServlet:
    - Line 56: PreparedStatement used for movieId(from MySQL database)
- PaymentServlet:
    - Line 59: PreparedStatement used for stored customer id (previously taken from MySQL database)
- ConfirmationServlet:
    - Line 48: PreparedStatement used for corresponding sale ids within a given range (from MySQL database and additional math)
> PreparedStatement used for anything that involved user input (ex: not used in parser as statements won't use user input)

### Parser Data Reporting:
- Will normally print "Insert # Attribute" for data entires (movies, stars, genres)
- Will print "# Exception (variable) Errors" that reports # of occurrences of a certain exception for a corresponding variable
    - All outputs are available for viewing in the terminal
    - "Load Data Complete!" is Output upon completion
    - There is a feature where the thread will stay open for an extra 5~ seconds after parser is finish running before being automatically shut down. Currently we are unaware of any fixes

  
### Parser Performance Tuning:
1. Using LOAD DATA.
    - Wrote information retrieved from xml files into corresponding csv files (5 total) that were inserted with LOAD DATA INFILE mySQL statements
    - LOAD DATA is faster when it comes to large data entires, eliminating the need for individual INSERT statements
2.  Using HashMap.
    - Loaded existing Genres and Stars to HashMaps once for easy accessability without having to constantly access MySQL
    - Made lookups very quick and doubles in preventing duplicates of Genres and Stars
    - **We did NOT use a HashMap for duplicate movies due to the fact that IDs were very different and Movies with all the same fields except IDs we're counted as unique**
        - ex: There is a Romeo and Juliet movie that shares the same Title, Director, and Name except the IDs are different. These are counted as unique. Existing movies have IDs that would not match those from the xml files so we save time by not comparing them.
