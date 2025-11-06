SELECT * FROM fts_table WHERE CONTAINS(col1, 'hello')
go

SELECT * FROM fts_table WHERE CONTAINS((col1, col2), 'hello')
go

-- not supported in bbf | review Manually in compass
select * from test_special_char_t where contains(name, 'one@#$two');
go

-- supported in bbf | review Manually in compass
select * from test_special_char_t where contains(name, '"one   # two ` three _ four"');
go

-- supported in bbf | Supported in compass
SELECT * FROM fts_contains_multicol_t WHERE CONTAINS((
        txt1, 
        txt2,
        txt3,
        txt4), 'genome AND DNA');
GO

-- supported in bbf | review Manually in compass
SELECT * FROM fts_contains_vu_t WHERE CONTAINS(txt, 'cancer and pain & stress')
GO

-- supported in bbf | review Manually in compass
SELECT * FROM fts_contains_vu_t WHERE CONTAINS(txt, 'Dupal And "Croissant*"')
GO

-- supported in bbf | review Manually in compass
SELECT * FROM fts_contains_vu_t WHERE CONTAINS(txt, 'French And "Croissant*" & tart')
GO
-- supported, review Manually
CREATE PROCEDURE fts_char_prefix_t_p1 AS (SELECT * FROM fts_char_prefix_t WHERE CONTAINS((main_story,
                                                                                                  industry_update,
                                                                                                  local_news,
                                                                                                  tech_highlight,
                                                                                                  community_event), ' "coast  *" '))
GO

-- supported in bbf | review Manually in compass
SELECT * FROM fts_char_prefix_t WHERE CONTAINS((main_story,industry_update,local_news,tech_highlight,community_event), ' "coast  *" ')
GO


-- supported in bbf | review Manually in compass
SELECT * FROM fts_char_prefix_t WHERE CONTAINS((main_story, industry_update, local_news), '"deforest*"')
GO

-- supported in bbf | review Manually in compass
SELECT * FROM fts_char_prefix_t WHERE CONTAINS(main_story, '"deforest*"')
GO

