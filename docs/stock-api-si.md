# Stock API - Frontend මාර්ගෝපදේශය

## Stock සහ Item සම්බන්ධතාව

එක් `Item` එකකට අදාළ `Stock` record එකක් පමණක් තිබිය හැකියි. Stock API එකේ `StockDetails` නැත; product එකේ ID එක `productId` ලෙස යවන්න. GET response එක product නමත් සමඟ flat object එකකි:

```json
{
  "stockId": 3,
  "type": "stock-in",
  "quantity": 28,
  "status": "ACTIVE",
  "date": "2026-10-20",
  "productId": 1,
  "productName": "A4 Color Print"
}
```

සියලු endpoint සඳහා:

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

## Stock එකක් save කිරීම

```http
POST /api/stock/save
```

```json
{
  "type": "stock-in",
  "quantity": 28,
  "status": "ACTIVE",
  "date": "2026-10-20",
  "productId": 1
}
```

- `type`: `stock-in` හෝ `stock-out`.
- `quantity`: 0 හෝ ඊට වැඩි පූර්ණ සංඛ්‍යාවක්.
- `date`: `YYYY-MM-DD` ආකෘතියෙන්.
- `productId`: පවතින Item එකක ID එක. එකම product එකට Stock record එකක් දැනටමත් තිබේ නම් `409 Conflict` ලැබේ.
- `productName` සහ `stockId` response එකේ එන fields; request එකේ යවන්න අවශ්‍ය නැහැ.

## Stock එක update කිරීම

```http
PUT /api/stock/update/{stockId}
```

Save request එකේ ආකෘතියම භාවිතා කරන්න. `productId` වෙනත් product එකකට මාරු කළ හැකියි, නමුත් එම product එකට වෙනත් Stock record එකක් නොතිබිය යුතුයි.

## Stock එක මකා දැමීම

```http
DELETE /api/stock/delete/{stockId}
```

Stock record එක පමණක් මැකේ; Item එක මැකෙන්නේ නැහැ.

## එක් Stock එකක් ලබාගැනීම

```http
GET /api/stock/{stockId}
```

## Stock ලැයිස්තුව, product search සහ filters

```http
GET /api/stock?page=0&size=10&search=A4&type=stock-in&status=ACTIVE&stockId=3&dateFrom=2026-10-01&dateTo=2026-10-31
```

| Parameter | අනිවාර්යද? | විස්තරය |
| --- | --- | --- |
| `page` | නැත | 0 සිට ආරම්භ වන පිටු අංකය. Default `0`. |
| `size` | නැත | පිටුවක records ගණන (`1`–`100`). Default `10`. |
| `search` | නැත | Stock ID, Product ID හෝ product නමින් සොයයි. |
| `type` | නැත | `stock-in` හෝ `stock-out` අනුව filter කරයි. |
| `status` | නැත | Stock status එක අනුව filter කරයි. |
| `stockId` | නැත | Stock ID එකට exact match කරයි. |
| `date` | නැත | දිනයකට exact match (`YYYY-MM-DD`). |
| `dateFrom` | නැත | දිනයේ ආරම්භක සීමාව, ඇතුළුව. |
| `dateTo` | නැත | දිනයේ අවසන් සීමාව, ඇතුළුව. |

`date` හෝ `dateFrom`/`dateTo` range එකෙන් එකක් භාවිතා කරන්න; දෙකම එකවර යවන්න එපා. `dateFrom` දිනය `dateTo` ට පසුව නම් `400 Bad Request` ලැබේ. Filters එකට භාවිතා කළ විට ඒවා සියල්ලටම ගැළපෙන records පමණක් ලැබේ. `search` product name එකේ කොටසක් හෝ Product ID/Stock ID අඩංගුදැයි සොයයි.

## අඩු stock

```http
GET /api/stock/low-stock?threshold=10&page=0&size=10
```

Frontend එකෙන් `threshold` යවන්න. `quantity < threshold` වන Stock records පමණක් ලැබේ. Threshold එකට සමාන quantity එකක් ප්‍රතිඵලයට ඇතුළත් නොවේ.

## Paginated response

Stock list සහ low-stock endpoint දෙකේම `content` තුළ flat Stock response objects ලැබේ:

```json
{
  "content": [
    {
      "stockId": 3,
      "type": "stock-in",
      "quantity": 28,
      "status": "ACTIVE",
      "date": "2026-10-20",
      "productId": 1,
      "productName": "A4 Color Print"
    }
  ],
  "currentPage": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "empty": false
}
```

## Frontend fetch උදාහරණය

```js
async function fetchStocks({
  token,
  page = 0,
  size = 10,
  search = "",
  type = "",
  status = "",
  stockId = "",
  date = "",
  dateFrom = "",
  dateTo = "",
}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  if (search.trim()) params.set("search", search.trim());
  if (type) params.set("type", type);
  if (status) params.set("status", status);
  if (stockId !== "") params.set("stockId", String(stockId));
  if (date) {
    params.set("date", date);
  } else {
    if (dateFrom) params.set("dateFrom", dateFrom);
    if (dateTo) params.set("dateTo", dateTo);
  }

  const response = await fetch(
    `${API_BASE_URL}/api/stock?${params.toString()}`,
    { headers: { Authorization: `Bearer ${token}` } },
  );
  if (!response.ok) {
    throw new Error(`Stock request failed: ${response.status}`);
  }
  return response.json();
}
```

Filter එකක් වෙනස් වූ විට page එක `0` කරන්න. HTML date input එකෙන් ලැබෙන `YYYY-MM-DD` අගය සෘජුව යවන්න.

## පවතින database දත්ත ගැන

මෙම වෙනසෙන් `stock` table එකට `quantity`, `date`, `product_id` fields සහ product එකකට unique constraint එකක් අවශ්‍ය වේ. පැරණි `stock_details` records නව Stock records බවට application එක ස්වයංක්‍රීයව පරිවර්තනය නොකරයි. පවතින දත්ත තිබේ නම් deployment එකට පෙර backup සහ අවශ්‍ය data migration එකක් කරන්න; පැරණි details table එක code එකෙන් ඉවත් කළත් DB table එක ස්වයංක්‍රීයව delete නොවේ.
