print("Running custom init script for sentiment_db...");

db = db.getSiblingDB("sentiment_db");  // switch to sentiment_db

// Collection: API_DATA 
db.api_data.createIndex({ "movie_id": 1 }, { unique: true });
db.api_data.createIndex({ "title": 1 }); 


// Collection: REVIEW_DATA 
db.reviews_data.createIndex({ "title": 1 }); 

print("Custom init complete. Ready for data collection.");
