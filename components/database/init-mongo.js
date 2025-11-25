print("Running custom init script for sentiment_db...");

db = db.getSiblingDB("sentiment_db");  // switch to sentiment_db

db.api_data.createIndex({ "movie_id": 1 }, { unique: true });

print("Custom init complete. Ready for data collection.");
