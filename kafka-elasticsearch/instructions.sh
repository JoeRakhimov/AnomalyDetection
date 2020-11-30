# in order to run elastic search, do the following commands:

# 1. Register on https://bonsai.io

# 2. create elasticsearch.properties file on kafka-elasticsearch folder
# file should include following lines without '#' sign and replace property values with credentials from bonsai.io
# hostname=replace_this_with_credentials_from_bonsai_io
# username=replace_this_with_credentials_from_bonsai_io
# password=replace_this_with_credentials_from_bonsai_io

# 3. Go to https://app.bonsai.io/clusters/replace_this_with_your_app_name_on_bonsai_io/console
# Open console
# Select PUT and enter:
# /balance
# click button 'RUN'
# this creates index 'balance'
# /twitter
# click button 'RUN'
# this creates index 'twitter'

# 4. Set ElasticSearchAllData.java's esConfigFile variable to absolute path to elasticsearch.properties file in your computer
# 5. Run ElasticSearchAllData.java main() function

# 6. # 4. Set ElasticSearchAllData.java's esConfigFile variable to absolute path to elasticsearch.properties file in your computer
# 7. Run ElasticSearchFilterData.java main() function

# 8. # 4. Set ElasticSearchTwitter.java's esConfigFile variable to absolute path to elasticsearch.properties file in your computer
# 9. Run ElasticSearchTwitter.java main() function





