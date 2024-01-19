USE moviedb;
DROP procedure IF EXISTS add_movie;
DELIMITER //

CREATE PROCEDURE add_movie(
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN star_birth_year INT,  -- Optional, default NULL
    IN genre_name VARCHAR(32)
)
BEGIN
    DECLARE existing_movie_id VARCHAR(10);
    DECLARE existing_star_id VARCHAR(10);
    DECLARE existing_genre_id INT;

    -- Check if the movie already exists
    SELECT id INTO existing_movie_id
    FROM movies
    WHERE title = movie_title AND year = movie_year AND director = movie_director;

    IF existing_movie_id IS NOT NULL THEN
        SELECT 'Movie already exists.' AS status_message;
    ELSE
        -- Movie does not exist: Find or insert the star and get the star_id
        SELECT id INTO existing_star_id FROM stars WHERE name = star_name;

        IF existing_star_id IS NULL THEN
            -- If the star doesn't exist, create one and insert into table
            -- create id
            SET existing_star_id = concat("nm", (SELECT MAX(substring(id, 3)) from stars) + 1);
            INSERT INTO stars (id, name, birthYear) VALUES (existing_star_id, star_name, star_birth_year);
        END IF;

        -- Find or insert the genre and get the genre_id
        SELECT id INTO existing_genre_id FROM genres WHERE name = genre_name;

        IF existing_genre_id IS NULL THEN
            -- If the genre doesn't exist, insert it
            INSERT INTO genres (name) VALUES (genre_name);
            -- Use LAST_INSERT_ID because genre_id is auto-incremented
            SET existing_genre_id = LAST_INSERT_ID();
        END IF;

        -- Create ID for movie
        SET @max_seq_number = COALESCE((SELECT MAX(CAST(SUBSTRING(id, 3) AS SIGNED)) + 1 FROM movies WHERE id LIKE 'tt%'),1 );
        SET existing_movie_id = CONCAT('tt', @max_seq_number);
        -- Insert the movie
        INSERT INTO movies (id, title, year, director) VALUES (existing_movie_id, movie_title, movie_year, movie_director);
        -- Link the movie to the star and genre
        INSERT INTO stars_in_movies (starId, movieId) VALUES (existing_star_id, existing_movie_id);
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (existing_genre_id, existing_movie_id);
        SELECT concat('SUCCESS! Movie Created with MovieId : ',existing_movie_id, ', StarId : ', existing_star_id, ' Genre Id : ', existing_genre_id) as status_message;
    END IF;
END //

DELIMITER ;



