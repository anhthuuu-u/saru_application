const express = require('express');
const app = express();
const morgan=require("morgan")
app.use(morgan("combined"))
const port = 3000;
app.get('/', (req, res) => {
  res.send('Hello World!');
});
app.listen(port, () => {
  console.log(`Example app listening at http://localhost:${port}`);
} );