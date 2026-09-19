CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    firebase_uid TEXT UNIQUE NOT NULL,
    email TEXT NOT NULL,
    first_name TEXT NOT NULL,
    surname TEXT NOT NULL,
    display_name TEXT NOT NULL,
    bio TEXT,
    avatar_url TEXT,
    friend_code TEXT UNIQUE NOT NULL,
    cooking_interests TEXT[] DEFAULT '{}',
    dietary_restrictions TEXT[] DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS recipe_books (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    cover_image_url TEXT,
    icon TEXT NOT NULL,
    colour TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS recipes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id UUID NOT NULL REFERENCES recipe_books(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    category TEXT,
    cuisine TEXT,
    food_type TEXT,
    difficulty TEXT,
    servings INTEGER DEFAULT 1,
    cook_time_minutes INTEGER DEFAULT 0,
    method TEXT,
    allergens TEXT[] DEFAULT '{}',
    is_private BOOLEAN DEFAULT true,
    is_favourite BOOLEAN DEFAULT false,
    forked_from_recipe_id UUID REFERENCES recipes(id),
    fork_count INTEGER DEFAULT 0,
    times_cooked INTEGER DEFAULT 0,
    image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS ingredients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    unit TEXT,
    sort_order INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS steps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    "order" INTEGER NOT NULL,
    instruction TEXT NOT NULL,
    timer_seconds INTEGER
);

CREATE TABLE IF NOT EXISTS pantry_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    unit TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shopping_list_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    unit TEXT,
    is_bought BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS cook_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    completed_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS streak_records (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    current_streak INTEGER DEFAULT 0,
    longest_streak INTEGER DEFAULT 0,
    last_cooked_date DATE
);

CREATE TABLE IF NOT EXISTS achievement_definitions (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT NOT NULL,
    condition_type TEXT NOT NULL,
    threshold_value INTEGER NOT NULL,
    icon_url TEXT
);

CREATE TABLE IF NOT EXISTS user_achievement_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id TEXT NOT NULL REFERENCES achievement_definitions(id),
    progress INTEGER DEFAULT 0,
    unlocked BOOLEAN DEFAULT false,
    unlocked_at TIMESTAMPTZ,
    UNIQUE (user_id, achievement_id)
);

CREATE TABLE IF NOT EXISTS friend_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    from_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    to_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status TEXT DEFAULT 'Pending',
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS friendships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_a_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_b_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS follows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    follower_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipe_id UUID NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5),
    text TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS notification_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fcm_token TEXT NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS notification_preferences (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    comment_notifications BOOLEAN DEFAULT true,
    friend_request_notifications BOOLEAN DEFAULT true,
    recipe_share_notifications BOOLEAN DEFAULT true
);

ALTER TABLE recipes ADD COLUMN IF NOT EXISTS image_url TEXT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'notification_tokens_fcm_token_key'
    ) THEN
        ALTER TABLE notification_tokens ADD CONSTRAINT notification_tokens_fcm_token_key UNIQUE (fcm_token);
    END IF;
END $$;

INSERT INTO achievement_definitions (id, name, description, category, condition_type, threshold_value) VALUES
    ('first_cook', 'First Cook', 'Cook your very first recipe', 'Cooking', 'recipesCooked', 1),
    ('streak_starter', 'Streak Starter', 'Maintain a 7-day cooking streak', 'Cooking', 'streakDays', 7),
    ('recipe_hoarder', 'Recipe Hoarder', 'Save 25 recipes to your cookbook', 'Contribution', 'recipesAdded', 25),
    ('community_star', 'Community Star', 'Get 50 cooks on your recipes', 'Contribution', 'cooksOnMyRecipes', 50),
    ('fork_master', 'Fork Master', 'Fork 10 public recipes', 'Contribution', 'forksCount', 10),
    ('pantry_pro', 'Pantry Pro', 'Add 20 pantry items', 'Contribution', 'pantryItemsAdded', 20),
    ('social_butterfly', 'Social Butterfly', 'Add 5 friends', 'Community', 'friendsAdded', 5),
    ('chef_extraordinaire', 'Chef Extraordinaire', 'Cook 50 different recipes', 'Community', 'uniqueRecipesCooked', 50)
ON CONFLICT (id) DO NOTHING;