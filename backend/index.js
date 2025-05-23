require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { MongoClient, ObjectId } = require('mongodb');
const app = express();

const PORT = process.env.PORT || 4001;
const MONGODB_URI = process.env.MONGODB_URI;

app.use(cors());
app.use(express.json());

let client;
let faqCollection;

async function connectDB() {
    client = new MongoClient(MONGODB_URI);
    await client.connect();
    const database = client.db('SaruData');
    faqCollection = database.collection('faqs');
    console.log('Kết nối MongoDB thành công');
}
connectDB();

// API: Lấy tất cả FAQ
app.get('/faqs', async (req, res) => {
    try {
        const faqs = await faqCollection.find({}).toArray();
        res.json(faqs);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// API: Thêm FAQ
app.post('/faqs', async (req, res) => {
    try {
        const { FaqID, FaqTitle, FaqContent } = req.body;
        const faq = { FaqID, FaqTitle, FaqContent };
        const result = await faqCollection.insertOne(faq);
        res.status(201).json({ ...faq, _id: result.insertedId });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// API: Xóa FAQ theo ID
app.delete('/faqs/:id', async (req, res) => {
    try {
        const result = await faqCollection.deleteOne({ _id: new ObjectId(req.params.id) });
        if (result.deletedCount === 0) {
            return res.status(404).json({ message: "FAQ không tồn tại" });
        }
        res.json({ message: "Đã xóa FAQ" });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.listen(PORT, () => {
    console.log(`🚀 Server đang chạy tại http://localhost:${PORT}`);
});
