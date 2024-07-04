ODIN
====

A web application that parses statistics data from Heritrix crawl reports and displays them in a charts.
Built using a GWT-based paradigm as a proof of concept.

Intended for use by the NLA web archiving team only.


Adding statistics data
----------------------

To add the statistics data for a new WDH harvest:

1. Copy the hosts-report.txt file into <odin_data_directory>/source_data/harvest_host_reports/ and rename it <yyyy>_<MM>.txt based on the year and month of the harvest (for example 2024_07.text).
2. Copy the mimetype-report.txt file into <odin_data_directory>/source_data/harvest_mime_reports/ and rename it <yyyy>_<MM>.txt based on the year and month of the harvest (for example 2024_07.text).
3. Refresh the web page to view the new data. The first time the new data is loaded, it will be parsed from the report files and cached as statistics data, so it may take a minute to load. After successfully loading the first time, loading will be almost instantaneous. Note: geolocation parsing may take several minutes.


Developing
----------

To develop ODIN, install the latest version of the GWT plugin for your IDE. 

1. Run the odin-server project (OdinServer.java) as a Java Application. Changes to server-side code will require this process to be restarted.
2. Run the odin-ui project as a GWT web application, with Super DEV Mode enabled. Example eclipse run configuration below:

![image](https://github.com/nla/odin/assets/168795533/d3b59c62-8f1f-44fb-b685-1612ffb14130)
![image](https://github.com/nla/odin/assets/168795533/9b701a7a-316b-4a0f-b90d-2d2cc4162df3)

3. In UIClientContext.java, change the variable DEV to true.
4. Navigate to http://127.0.0.1:8888/ in your browser.
5. Any changes made to UI code only requires the browser to be refreshed.
