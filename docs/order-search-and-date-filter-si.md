# Orders සෙවීම සහ දිනය අනුව පෙරීම - Frontend මාර්ගෝපදේශය

## API endpoint

`GET /api/order`

### Headers

```http
Authorization: Bearer <JWT_TOKEN>
```

### Query parameters

| Parameter | වර්ගය | අනිවාර්යද? | විස්තරය |
| --- | --- | --- | --- |
| `page` | number | නැත | පිටු අංකය 0 සිට ආරම්භ වේ. Default `0`. |
| `size` | number | නැත | එක් පිටුවක orders ගණන. `1` සිට `100` දක්වා; default `10`. |
| `search` | string | නැත | Order ID, order දිනය හෝ order detail එකක product නම, product ID, detail ID, quantity, total සොයයි. |
| `status` | string | නැත | Status එකෙන් පෙරයි. `ALL STATUS` හෝ හිස් අගයක් යැවූ විට status filter එක නොයෙදේ. |
| `dateFrom` | string (`YYYY-MM-DD`) | නැත | Date range එකේ ආරම්භක දිනය. එම දිනයේ ආරම්භයේ සිට ඇතුළත් වේ. |
| `dateTo` | string (`YYYY-MM-DD`) | නැත | Date range එකේ අවසන් දිනය. එම දිනයේ මුළු දවසම ඇතුළත් වේ. |

`dateFrom` සහ `dateTo` දෙකම එකවර හෝ එකක් පමණක් යැවිය හැකියි. දෙකම යැවූ විට `dateFrom` දිනය `dateTo` දිනයට පසුව නොවිය යුතුයි. සෙවුම් පෙළ, status සහ දිනයේ සීමා එකට යැවූ විට සියලු filter කොන්දේසි එකවර සපුරන orders පමණක් ලැබේ. දින පෙරීම `search` parameter එකෙන් වෙන්ව සිදු වේ.

### Request උදාහරණ

2026 මාර්තු 1 සිට මාර්තු 31 දක්වා orders:

```http
GET /api/order?page=0&size=10&dateFrom=2026-03-01&dateTo=2026-03-31
```

එම කාලය තුළ product එකක නම `paper` අඩංගු orders පමණක්, `DELIVERED` status සමඟ:

```http
GET /api/order?page=0&size=10&search=paper&status=DELIVERED&dateFrom=2026-03-01&dateTo=2026-03-31
```

## Frontend JavaScript උදාහරණය

```js
async function getOrders({
  token,
  page = 0,
  size = 10,
  search = "",
  status = "",
  dateFrom = "",
  dateTo = "",
}) {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  if (search.trim()) params.set("search", search.trim());
  if (status && status !== "ALL STATUS") params.set("status", status);
  if (dateFrom) params.set("dateFrom", dateFrom); // YYYY-MM-DD
  if (dateTo) params.set("dateTo", dateTo); // YYYY-MM-DD

  const response = await fetch(
    `${API_BASE_URL}/api/order?${params.toString()}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
  );

  if (!response.ok) {
    throw new Error(`Orders request failed: ${response.status}`);
  }

  return response.json();
}
```

HTML date input-ууд (`<input type="date">`) `YYYY-MM-DD` хэлбэрээр утга өгдөг тул утгыг өөрчлөлгүйгээр `dateFrom` болон `dateTo` параметрүүдэд дамжуулන්න. Filter එකක් වෙනස් වූ විට `page` නැවත `0` කරන්න.

## Response හැඩය

Response එක `PaginationResponse<OrderDto>` වේ. Orders ලැයිස්තුව `content` තුළ ඇත.

```json
{
  "content": [
    {
      "orderId": 42,
      "totalPrice": 2500.00,
      "orderType": "ONLINE",
      "date": "2026-03-15T10:30:00",
      "status": "DELIVERED",
      "userId": 7,
      "orderDetails": [
        {
          "orderDetailsId": 103,
          "qty": 2,
          "total": 2500.00,
          "productId": 12,
          "orderId": 42
        }
      ]
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

## දෝෂ ප්‍රතිචාර

- වැරදි දිනයක් (`YYYY-MM-DD` නොවන අගයක්) යැවූ විට `400 Bad Request`.
- `dateFrom` දිනය `dateTo` ට පසුව නම් `400 Bad Request`.
- Token එක නොමැති හෝ අවලංගු නම් `401 Unauthorized`.
