IF DB_ID(N'Cinema') IS NULL
    CREATE DATABASE Cinema;
GO

USE Cinema;
GO

IF OBJECT_ID(N'dbo.Seante', N'U') IS NOT NULL
DROP TABLE dbo.Seante;
GO

CREATE TABLE dbo.Seante (
                            Id           INT IDENTITY(1,1) NOT NULL,
                            Film         NVARCHAR(100)  NOT NULL,
                            Sala         NVARCHAR(20)   NOT NULL,
                            Ora          CHAR(5)        NOT NULL,      -- HH:mm
                            Pret         DECIMAL(6,2)   NOT NULL,
                            LocuriLibere INT            NOT NULL,
    -- câmpuri extra pentru design
                            Gen          NVARCHAR(30)   NOT NULL,
                            DurataMin    INT            NOT NULL,
                            Rating       DECIMAL(3,1)   NOT NULL,
                            Descriere    NVARCHAR(500)  NULL,
                            Poster       NVARCHAR(100)  NULL,

                            CONSTRAINT PK_Seante         PRIMARY KEY (Id),
                            CONSTRAINT UQ_Seante_Sala_Ora UNIQUE (Sala, Ora),
                            CONSTRAINT CK_Seante_Pret    CHECK (Pret BETWEEN 30 AND 300),
                            CONSTRAINT CK_Seante_Locuri  CHECK (LocuriLibere BETWEEN 0 AND 400)
);
GO

INSERT INTO dbo.Seante (Film, Sala, Ora, Pret, LocuriLibere, Gen, DurataMin, Rating, Descriere, Poster) VALUES
(N'Dune: Partea a Doua', N'Sala 1', '18:30',  90.00, 120, N'SF',        166, 8.5,
 N'Paul Atreides se alătură fremenilor și pornește pe calea răzbunării împotriva celor care i-au distrus familia.', 'dune.jpg'),
(N'Oppenheimer',          N'Sala 2', '19:00',  85.00,   0, N'Dramă',     180, 8.3,
 N'Povestea fizicianului care a condus proiectul Manhattan și a schimbat cursul istoriei.', 'oppenheimer.jpg'),
(N'Inside Out 2',         N'Sala 3', '16:00',  60.00,  45, N'Animație',   96, 7.6,
 N'Riley devine adolescentă, iar în mintea ei apar emoții noi, care răstoarnă tot ce știa.', 'insideout2.jpg'),
(N'Joker',                N'Sala 2', '21:30',  80.00,   8, N'Thriller',  122, 8.4,
 N'Un comedian eșuat din Gotham alunecă treptat spre nebunie și crimă.', 'joker.jpg'),
(N'Interstellar',         N'Sala 1', '21:45',  95.00, 200, N'SF',        169, 8.7,
 N'Un grup de exploratori călătorește prin gaura de vierme pentru a găsi o nouă casă omenirii.', 'interstellar.jpg'),
(N'Mad Max: Fury Road',   N'Sala 4', '20:15',  75.00,  60, N'Acțiune',   120, 8.1,
 N'O urmărire nebună prin deșert, în care Furiosa încearcă să scape de un tiran.', 'madmax.jpg'),
(N'Kung Fu Panda 4',      N'Sala 3', '14:00',  55.00, 150, N'Animație',   94, 6.9,
 N'Po trebuie să-și aleagă un succesor și se confruntă cu o vrăjitoare care schimbă înfățișarea.', 'kungfupanda4.jpg'),
(N'The Batman',           N'Sala 4', '22:45', 100.00,   5, N'Acțiune',   176, 7.8,
 N'Batman anchetează o serie de crime și descoperă corupția din inima orașului Gotham.', 'batman.jpg');
GO

SELECT * FROM dbo.Seante;
GO