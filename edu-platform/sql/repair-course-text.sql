-- Repair mojibake course/category seed data in a running local MySQL container.
-- This file intentionally uses UTF-8 hex literals so it survives Windows shell encodings.
-- Usage:
--   docker cp sql\repair-course-text.sql edu-mysql:/tmp/repair-course-text.sql
--   docker exec edu-mysql sh -c "mysql --default-character-set=utf8mb4 -uroot -pedu123456 < /tmp/repair-course-text.sql"

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE edu_course;

UPDATE t_category SET name = CONVERT(0xe7bc96e7a88be5bc80e58f91 USING utf8mb4), icon = CONVERT(0xf09f92bb USING utf8mb4) WHERE id = 1;
UPDATE t_category SET name = CONVERT(0xe695b0e68daee7a791e5ada6 USING utf8mb4), icon = CONVERT(0xf09f938a USING utf8mb4) WHERE id = 2;
UPDATE t_category SET name = CONVERT(0xe4babae5b7a5e699bae883bd USING utf8mb4), icon = CONVERT(0xf09fa496 USING utf8mb4) WHERE id = 3;
UPDATE t_category SET name = CONVERT(0xe4baa7e59381e8aebee8aea1 USING utf8mb4), icon = CONVERT(0xf09f8ea8 USING utf8mb4) WHERE id = 4;
UPDATE t_category SET name = CONVERT(0xe8818ce59cbae68a80e883bd USING utf8mb4), icon = CONVERT(0xf09f92bc USING utf8mb4) WHERE id = 5;
UPDATE t_category SET name = CONVERT(0xe8afade8a880e5ada6e4b9a0 USING utf8mb4), icon = CONVERT(0xf09f8c90 USING utf8mb4) WHERE id = 6;

UPDATE t_course
SET title = CONVERT(0x537072696e6720426f6f7420332e7820e5beaee69c8de58aa1e5ae9ee68898 USING utf8mb4),
    description = CONVERT(0xe4bb8ee99bb6e5bc80e5a78be69e84e5bbbae7949fe4baa7e7baa7e5beaee69c8de58aa1e7b3bbe7bb9fefbc8ce6b6b5e79b9620537072696e6720436c6f756420e585a8e5aeb6e6a1b6 USING utf8mb4),
    teacher_name = 'teacher01',
    tags = CONVERT(0x537072696e6720426f6f742ce5beaee69c8de58aa12c4a617661 USING utf8mb4)
WHERE id = 1;

UPDATE t_course
SET title = CONVERT(0x5675652033202b205479706553637269707420e585a8e6a088e5bc80e58f91 USING utf8mb4),
    description = CONVERT(0xe78eb0e4bba3e5898de7abafe5b7a5e7a88be58c96e5ae9ee8b7b5efbc8ce4bb8ee59fbae7a180e588b0e9a1b9e79baee5ae9ee68898 USING utf8mb4),
    teacher_name = 'teacher01',
    tags = CONVERT(0x567565332c547970655363726970742ce5898de7abaf USING utf8mb4)
WHERE id = 2;

UPDATE t_course
SET title = CONVERT(0x507974686f6e20e695b0e68daee58886e69e90e4b88ee58fafe8a786e58c96 USING utf8mb4),
    description = CONVERT(0x50616e646173e380814d6174706c6f746c6962e38081536561626f726e20e695b0e68daee5a484e79086e585a8e6b581e7a88b USING utf8mb4),
    teacher_name = 'teacher01',
    tags = CONVERT(0x507974686f6e2ce695b0e68daee58886e69e902ce58fafe8a786e58c96 USING utf8mb4)
WHERE id = 3;

UPDATE t_course
SET title = CONVERT(0xe6b7b1e5baa6e5ada6e4b9a0e585a5e997a8e4b88ee5ae9ee8b7b5 USING utf8mb4),
    description = CONVERT(0xe7a59ee7bb8fe7bd91e7bb9ce58e9fe79086e380815079546f72636820e6a186e69eb6e3808143562f4e4c5020e9a1b9e79baee5ae9ee68898 USING utf8mb4),
    teacher_name = 'teacher01',
    tags = CONVERT(0xe6b7b1e5baa6e5ada6e4b9a02c5079546f7263682c4149 USING utf8mb4)
WHERE id = 4;

UPDATE t_course
SET title = CONVERT(0x446f636b65722026204b756265726e6574657320e4ba91e58e9fe7949f USING utf8mb4),
    description = CONVERT(0xe5aeb9e599a8e58c96e983a8e7bdb2e380814b387320e99b86e7bea4e7aea1e79086e3808143492f434420e6b581e6b0b4e7babf USING utf8mb4),
    teacher_name = 'teacher01',
    tags = CONVERT(0x446f636b65722c4b38732ce4ba91e58e9fe7949f USING utf8mb4)
WHERE id = 5;

UPDATE t_course
SET title = CONVERT(0xe4baa7e59381e7bb8fe79086e4bb8ee585a5e997a8e588b0e7b2bee9809a USING utf8mb4),
    description = CONVERT(0xe99c80e6b182e58886e69e90e38081e58e9fe59e8be8aebee8aea1e38081e695b0e68daee9a9b1e58aa8e586b3e7ad96e585a8e6b581e7a88b USING utf8mb4),
    teacher_name = 'teacher01',
    tags = CONVERT(0xe4baa7e59381e7bb8fe790862ce99c80e6b182e58886e69e90 USING utf8mb4)
WHERE id = 6;

UPDATE t_course
SET title = CONVERT(0x4d7953514c20e680a7e883bde4bc98e58c96e5ae9ee68898 USING utf8mb4),
    description = CONVERT(0xe7b4a2e5bc95e4bc98e58c96e38081e69fa5e8afa2e8b083e4bc98e38081e58886e5ba93e58886e8a1a8e5ae9ee8b7b5 USING utf8mb4),
    teacher_name = 'teacher01',
    tags = CONVERT(0x4d7953514c2ce680a7e883bde4bc98e58c962ce695b0e68daee5ba93 USING utf8mb4)
WHERE id = 7;

UPDATE t_course
SET title = CONVERT(0xe88bb1e8afade58fa3e8afade6b581e588a9e8afb4 USING utf8mb4),
    description = CONVERT(0xe5a496e69599e68385e699afe5afb9e8af9defbc8ce68f90e58d87e59586e58aa1e5928ce697a5e5b8b8e88bb1e8afade8a1a8e8bebe USING utf8mb4),
    teacher_name = 'teacher01',
    tags = CONVERT(0xe88bb1e8afad2ce58fa3e8afad2ce59586e58aa1e88bb1e8afad USING utf8mb4)
WHERE id = 8;
