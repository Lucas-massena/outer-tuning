CREATE SEQUENCE [dbo].[S_EXPERIMENT_NUMBER] 
 AS [int]
 START WITH 0
 INCREMENT BY 1
 MINVALUE -2147483648
 MAXVALUE 2147483647
 CACHE 
GO


drop table dbo.experiment;

CREATE TABLE [dbo].[experiment](
	[query_name] [nchar](50) NULL,
	[mean_cost] [numeric](18, 2) NULL,
	[experiment_number] [int] NULL,
	[id] [int] IDENTITY(1,1) PRIMARY KEY,
	[date_experiment] [date] NULL,
	[test_name] [nchar](50) NULL,
	[parameters] [text] NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO