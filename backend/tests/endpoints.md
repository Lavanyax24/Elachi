# Manual endpoint tests (curl)

Every route except /, /health and POST /api/users/sync needs a real
Firebase ID token in the Authorization: Bearer <token> header. The easiest
way to get one for manual testing: log in through the Android app (or a
throwaway script using the Firebase Web SDK), then read the token that
AuthRepository/RetrofitClient's auth interceptor attaches — or add a
temporary Log.d("token", idToken) line in the app while testing locally.

Replace:
- $BASE — http://localhost:3000 locally, or your Render URL
- $TOKEN — a real Firebase ID token
- $BOOK_ID, $RECIPE_ID — real ids returned by earlier calls

bash
export BASE=http://localhost:3000
export TOKEN="paste-a-real-firebase-id-token-here"


## Health check (no auth)

bash
curl -s $BASE/health


## users/sync — creates or fetches the local profile row

bash
curl -s -X POST $BASE/api/users/sync \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firebaseUid":"REPLACE_WITH_YOUR_FIREBASE_UID","email":"me@example.com","firstName":"Diya","surname":"Lahka"}'


## users/me

bash
curl -s $BASE/api/users/me -H "Authorization: Bearer $TOKEN"

curl -s -X PATCH $BASE/api/users/me \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"bio":"I love cooking"}'


## books

bash
curl -s -X POST $BASE/api/books \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Weeknight Dinners","description":"Quick meals","icon":"🍜","colour":"#2F5233"}'

curl -s $BASE/api/books -H "Authorization: Bearer $TOKEN"


## recipes

bash
curl -s -X POST $BASE/api/recipes \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "bookId":"'$BOOK_ID'","title":"Fried Rice","category":"Dinner","cuisine":"Asian",
    "servings":2,"cookTimeMinutes":20,"method":"Stovetop","isPrivate":true,
    "ingredients":[{"name":"rice","quantity":2,"unit":"cup"}],
    "steps":[{"order":1,"instruction":"Cook the rice."}]
  }'

curl -s $BASE/api/recipes -H "Authorization: Bearer $TOKEN"


## recipes/pantry-health

bash
curl -s $BASE/api/recipes/pantry-health -H "Authorization: Bearer $TOKEN"


## pantry

bash
curl -s -X POST $BASE/api/pantry \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"rice","quantity":2,"unit":"kg"}'

curl -s $BASE/api/pantry -H "Authorization: Bearer $TOKEN"


## shopping-list

bash
curl -s -X POST $BASE/api/shopping-list \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Eggs","quantity":6,"unit":"pcs"}'

curl -s $BASE/api/shopping-list -H "Authorization: Bearer $TOKEN"


## cook-sessions

bash
curl -s -X POST $BASE/api/cook-sessions \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"recipeId":"'$RECIPE_ID'"}'


## achievements

bash
curl -s $BASE/api/achievements -H "Authorization: Bearer $TOKEN"


## streaks

bash
curl -s $BASE/api/streaks -H "Authorization: Bearer $TOKEN"


## chat (AI Chef Assistant — needs COHERE_API_KEY set)

bash
curl -s -X POST $BASE/api/chat \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"message":"What can I make with rice and eggs?"}'


## recipes/parse-text (AI OCR structuring — needs COHERE_API_KEY set)

bash
curl -s -X POST $BASE/api/recipes/parse-text \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"rawText":"Fried Rice\n2 cups rice\n1 egg\n1. Cook rice. 2. Fry egg. 3. Mix."}'