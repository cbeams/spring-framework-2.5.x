USE jpetstore;

CREATE TABLE IF NOT EXISTS supplier (
   suppid int NOT NULL, 
    name varchar(80) null,
    status varchar(2) not null,
    addr1 varchar(80) null,
    addr2 varchar(80) null,
    city varchar(80) null,
    state varchar(80) null,
    zip varchar(5) null,
    phone varchar(80) null,  
PRIMARY KEY (suppid)) 
TYPE=INNODB 
MIN_ROWS=0 
MAX_ROWS=1000 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Cadastro de Fornecedores';

CREATE TABLE IF NOT EXISTS signon (
    username varchar(25) not null,
    password varchar(25)  not null,  
PRIMARY KEY (username)) 
TYPE=INNODB 
MIN_ROWS=0 
MAX_ROWS=1000  
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Cadastro de usuários';

CREATE TABLE IF NOT EXISTS account (
    userid varchar(80) not null,
    email varchar(80) not null,
    firstname varchar(80) not null,
    lastname varchar(80) not null,
    status varchar(2)  null,
    addr1 varchar(80) not null,
    addr2 varchar(40) null,
    city varchar(80) not  null,
    state varchar(80) not null,
    zip varchar(20) not null,
    country varchar(20) not null,
    phone varchar(80) not null,
PRIMARY KEY (userid) )
TYPE=INNODB
MIN_ROWS=0 
MAX_ROWS=1000  
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Cadastro de Contas';

CREATE TABLE IF NOT EXISTS profile (
    userid varchar(80) not null,
    langpref varchar(80) not null,
    favcategory varchar(30),
    mylistopt bool,
    banneropt bool,
PRIMARY KEY (userid) )
TYPE=INNODB 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Cadastro de Perfis';

CREATE TABLE IF NOT EXISTS bannerdata (
    favcategory varchar(80) not null,
    bannername varchar(255)  null, 
PRIMARY KEY (favcategory))
TYPE=INNODB 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Banner Data';

CREATE TABLE IF NOT EXISTS orders (
      orderid int not null,
      userid varchar(80) not null,
      orderdate date not null,
      shipaddr1 varchar(80) not null,
      shipaddr2 varchar(80) null,
      shipcity varchar(80) not null,
      shipstate varchar(80) not null,
      shipzip varchar(20) not null,
      shipcountry varchar(20) not null,
      billaddr1 varchar(80) not null,
      billaddr2 varchar(80)  null,
      billcity varchar(80) not null,
      billstate varchar(80) not null,
      billzip varchar(20) not null,
      billcountry varchar(20) not null,
      courier varchar(80) not null,
      totalprice decimal(10,2) not null,
      billtofirstname varchar(80) not null,
      billtolastname varchar(80) not null,
      shiptofirstname varchar(80) not null,
      shiptolastname varchar(80) not null,
      creditcard varchar(80) not null,
      exprdate varchar(7) not null,
      cardtype varchar(80) not null,
      locale varchar(80) not null,
PRIMARY KEY (orderid) )
TYPE=INNODB 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Cadastro de pedidos';

CREATE TABLE IF NOT EXISTS orderstatus (
      orderid int not null,
      linenum int not null,
      timestamp date not null,
      status varchar(2) not null,
PRIMARY KEY (orderid, linenum) )
TYPE=INNODB 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Status de pedidos';

CREATE TABLE IF NOT EXISTS lineitem (
      orderid int not null,
      linenum int not null,
      itemid varchar(10) not null,
      quantity int not null,
      unitprice decimal(10,2) not null,
PRIMARY KEY (orderid, linenum) )
TYPE=INNODB 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Line Item';

CREATE TABLE IF NOT EXISTS category (
	catid varchar(10) not null,
	name varchar(80) null,
	descn varchar(255) null,
PRIMARY KEY (catid) )
TYPE=INNODB 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Categorias';

CREATE TABLE IF NOT EXISTS product (
    productid varchar(10) not null,
    category varchar(10) not null,
    name varchar(80) null,
    descn varchar(255) null,
PRIMARY KEY (productid) )
TYPE=INNODB 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Categorias';

ALTER TABLE product 
	ADD INDEX productCat(category);

ALTER TABLE product 
	ADD INDEX productName(name);

ALTER TABLE category 
	ADD INDEX ixCategoryProduct(catid);

ALTER TABLE product  ADD FOREIGN KEY (category) 
         REFERENCES category(catid) 
         ON DELETE RESTRICT 
         ON UPDATE RESTRICT;

CREATE TABLE IF NOT EXISTS item (
    itemid varchar(10) not null,
    productid varchar(10) not null,
    listprice decimal(10,2) null,
    unitcost decimal(10,2) null,
    supplier int null,
    status varchar(2) null,
    attr1 varchar(80) null,
    attr2 varchar(80) null,
    attr3 varchar(80) null,
    attr4 varchar(80) null,
    attr5 varchar(80) null,
PRIMARY KEY (itemid) )
TYPE=INNODB 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Itens';

ALTER TABLE item 
	ADD INDEX itemProd(productid);

ALTER TABLE item ADD FOREIGN KEY (productid) 
         REFERENCES product(productid) 
         ON DELETE RESTRICT 
         ON UPDATE RESTRICT;

ALTER TABLE item ADD FOREIGN KEY (supplier) 
         REFERENCES supplier(suppid) 
         ON DELETE RESTRICT 
         ON UPDATE RESTRICT;

CREATE TABLE IF NOT EXISTS inventory (
    itemid varchar(10) not null,
    qty int not null,
PRIMARY KEY (itemid) )
TYPE=INNODB 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Inventory';

CREATE TABLE IF NOT EXISTS sequence (
    name               varchar(30)  not null,
    nextid             int          not null,
PRIMARY KEY (name) )
TYPE=INNODB 
PACK_KEYS=DEFAULT 
ROW_FORMAT=DEFAULT 
COMMENT='Inventory';
