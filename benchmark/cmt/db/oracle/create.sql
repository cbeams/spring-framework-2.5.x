
drop table orders;
drop table items;
drop table users;


CREATE TABLE users (
	ID NUMERIC PRIMARY KEY,
	forename VARCHAR(32),
	surname VARCHAR(32)
);


create table items (
	id numeric primary key,
	name varchar(55) not null,
	stock numeric
);

create table orders (
	id numeric primary key,
	user_id numeric references users,
	item_id numeric references items,
	quantity numeric
);



CREATE SEQUENCE ORDER_SEQ 
	START WITH 10
	INCREMENT BY 1 
NOMAXVALUE;



create or replace
procedure place_order(user_id in number, item_id in number, quantity in number, new_order_id out number)
as 
begin
	-- Get a new pk for the order table
	select order_seq.nextval into new_order_id from dual;
	
	-- Create a new order
	insert into orders (id, user_id, item_id, quantity) 
		values (new_order_id, user_id, item_id, quantity);

	update items set stock = stock - quantity where id = item_id;
	

end;
/


insert into users (id, forename, surname)
	values (1, 'rod', 'johnson');
	
insert into items (id, name, stock)
	values (1, 'widget', 1000);
	
insert into orders (id, user_id, item_id, quantity)
	values (1, 1, 1, 5);
	
	
commit;



select count(*) from orders where id > 10000;


delete from orders where id > 10000;
commit;