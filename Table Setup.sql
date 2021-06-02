create database if not exists `POS`;
use `POS`;

-- create database if not exists `POS_TEST`;
-- use `POS_TEST`;

-- Remove constraints on foreign keys to allow dropping of tables
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE if exists `users`;
CREATE TABLE `users` (
    userID VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    role INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (userID)
);
INSERT INTO users VALUES ('mng1', 'mngpass', '2', 'John');
INSERT INTO users VALUES ('serv1', 'servpass', '1', 'Jane');
INSERT INTO users VALUES ('chef1', 'chefpass', '0', 'Bob');

drop table if exists `order`;
CREATE TABLE `order` (
    orderID INT8 UNSIGNED AUTO_INCREMENT NOT NULL,
    placedBy VARCHAR(50) NOT NULL,
    input_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completedBy VARCHAR(50) DEFAULT NULL,
    complete_time DATETIME DEFAULT NULL,
    deliveredBy VARCHAR(50) DEFAULT NULL,
    delivered_time DATETIME DEFAULT NULL,
    PRIMARY KEY (orderID),
    FOREIGN KEY (placedBy)
        REFERENCES users (userID)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (completedBy)
        REFERENCES users (userID)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (deliveredBy)
        REFERENCES users (userID)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

drop table if exists `inventory`;
CREATE TABLE `inventory` (
    name VARCHAR(50) NOT NULL,
    amount INT NOT NULL DEFAULT 0,
    desired_amount INT NOT NULL,
    PRIMARY KEY (name)
);
insert into inventory values ('Hamburger Bun', 50, 50);
insert into inventory values ('Hamburger Patty', 50, 50);
insert into inventory values ('Cheese', 50, 50);

drop table if exists `customization`;
CREATE TABLE `customization` (
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (name)
);
insert into customization values ('Cheese');
insert into customization values ('Doneness');

drop table if exists `customization_option`;
CREATE TABLE `customization_option` (
    name VARCHAR(50) NOT NULL,
    optionID INT NOT NULL,
    option_name VARCHAR(50) NOT NULL,
    price DECIMAL(13,2) NOT NULL,
    PRIMARY KEY (name , optionID),
    FOREIGN KEY (name)
        REFERENCES `customization` (name)
        ON UPDATE CASCADE ON DELETE CASCADE
);
insert into customization_option values ('Cheese', 0, 'No Cheese', 0);
insert into customization_option values ('Cheese', 1, 'Cheese', 1);
insert into customization_option values ('Doneness', 0, 'Rare', 0);
insert into customization_option values ('Doneness', 1, 'Medium-Rare', 0);
insert into customization_option values ('Doneness', 2, 'Medium', 0);
insert into customization_option values ('Doneness', 3, 'Medium-Well', 0);
insert into customization_option values ('Doneness', 4, 'Well-Done', 0);


drop table if EXISTS `menu_item`;
CREATE TABLE `menu_item` (
    name VARCHAR(50) NOT NULL,
    active BOOL NOT NULL DEFAULT TRUE,
    price DECIMAL(13,2) NOT NULL,
    PRIMARY KEY (name)
);
INSERT INTO menu_item (`name`) VALUES ('Hamburger');

drop table if exists `item`;
CREATE TABLE `item` (
    orderID INT8 UNSIGNED NOT NULL,
    item_num INT UNSIGNED NOT NULL,
    menu_item_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (orderID , item_num),
    FOREIGN KEY (orderID)
        REFERENCES `order` (orderID)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (menu_item_name)
        REFERENCES menu_item (name)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

drop table if exists `applied_customization`;
CREATE TABLE `applied_customization` (
    item_num INT UNSIGNED NOT NULL,
    orderID INT8 UNSIGNED NOT NULL,
    customization_name VARCHAR(50) NOT NULL,
    optionID INT NOT NULL,
    PRIMARY KEY (orderID , item_num , customization_name),
    FOREIGN KEY (orderID, item_num)
        REFERENCES item (orderID, item_num)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (customization_name, optionID)
        REFERENCES customization_option (name, optionID)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

drop table if exists `available`;
CREATE TABLE `available` (
    item_name VARCHAR(50) NOT NULL,
    customization_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (item_name , customization_name),
    FOREIGN KEY (item_name)
        REFERENCES menu_item (name)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (customization_name)
        REFERENCES customization (name)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
insert into available values ('Hamburger', 'Cheese');
insert into available values ('Hamburger', 'Doneness');

drop table if exists `item_uses`;
CREATE TABLE `item_uses` (
    item_name VARCHAR(50) NOT NULL,
    inventory_name VARCHAR(50) NOT NULL,
    amount INT NOT NULL DEFAULT 1,
    PRIMARY KEY (item_name , inventory_name),
    FOREIGN KEY (item_name)
        REFERENCES menu_item (name)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (inventory_name)
        REFERENCES inventory (name)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
insert into item_uses values ('Hamburger', 'Hamburger Bun', 1);
insert into item_uses values ('Hamburger', 'Hamburger Patty', 1);

drop table if exists `customization_uses`;
CREATE TABLE `customization_uses` (
    customization_name VARCHAR(50) NOT NULL,
    customization_option INT NOT NULL,
    inventory_name VARCHAR(50) NOT NULL,
    amount INT NOT NULL DEFAULT 1,
    PRIMARY KEY (customization_name , customization_option , inventory_name),
    FOREIGN KEY (customization_name , customization_option)
        REFERENCES customization_option (name , optionID)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (inventory_name)
        REFERENCES inventory (name)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
insert into customization_uses values ('Cheese', 1, 'Cheese', 1);

drop table if exists `inventory_last_balanced`;
CREATE TABLE `inventory_last_balanced` (
    date DATETIME NOT NULL PRIMARY KEY
);

drop table if exists `inventory_update`;
CREATE TABLE `inventory_update` (
    name VARCHAR(50) NOT NULL,
    update_amount INT NOT NULL,
    date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (name , date),
    FOREIGN KEY (name)
        REFERENCES inventory (name)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Reinstate foreign key constraints
SET FOREIGN_KEY_CHECKS=1;

-- Some dummy data
INSERT INTO `order` values(1, 'serv1', current_timestamp() - INTERVAL 6 day, 'chef1', current_timestamp() - INTERVAL 6 day + INTERVAL 5 MINUTE, 'serv1', current_timestamp() - INTERVAL 6 day + INTERVAL 10 MINUTE);
INSERT INTO `order` values(2, 'serv1', current_timestamp() - INTERVAL 5 day, 'chef1', current_timestamp() - INTERVAL 5 day + INTERVAL 5 MINUTE, 'serv1', current_timestamp() - INTERVAL 5 day + INTERVAL 10 MINUTE);
INSERT INTO `order` values(3, 'serv1', current_timestamp() - INTERVAL 4 day, 'chef1', current_timestamp() - INTERVAL 4 day + INTERVAL 5 MINUTE, null, null);
INSERT INTO `order` values(4, 'serv1', current_timestamp() - INTERVAL 3 day, NULL, NULL, null, null);
INSERT INTO `order` values(5, 'serv1', current_timestamp() - INTERVAL 2 day, NULL, NULL, null, null);
INSERT INTO `order` values(6, 'serv1', current_timestamp() - INTERVAL 1 day, NULL, NULL, null, null);
INSERT INTO `order` values(7, 'serv1', current_timestamp() - INTERVAL 0 day, NULL, NULL, null, null);
insert into `item` values(1,1,'Hamburger');
insert into `applied_customization` values(1, 1, 'Doneness', 4);
insert into `applied_customization` values(1, 1, 'Cheese', 1);
insert into `item` values(2,1,'Hamburger');
insert into `applied_customization` values(1, 2, 'Doneness', 3);
insert into `applied_customization` values(1, 2, 'Cheese', 1);
insert into `item` values(2,2,'Hamburger');
insert into `applied_customization` values(2, 2, 'Doneness', 2);
insert into `applied_customization` values(2, 2, 'Cheese', 1);
insert into `item` values(3,1,'Hamburger');
insert into `applied_customization` values(1, 3, 'Doneness', 0);
insert into `item` values(4,1,'Hamburger');
insert into `applied_customization` values(1, 4, 'Doneness', 4);
insert into `item` values(4,2,'Hamburger');
insert into `applied_customization` values(2, 4, 'Doneness', 0);
insert into `applied_customization` values(2, 4, 'Cheese', 1);
insert into `item` values(4,3,'Hamburger');
insert into `applied_customization` values(3, 4, 'Doneness', 3);
insert into `item` values(5,1,'Hamburger');
insert into `applied_customization` values(1, 5, 'Doneness', 4);
insert into `item` values(6,1,'Hamburger');
insert into `applied_customization` values(1, 6, 'Doneness', 2);
insert into `applied_customization` values(1, 6, 'Cheese', 1);
insert into `item` values(6,2,'Hamburger');
insert into `applied_customization` values(2, 6, 'Doneness', 1);
insert into `item` values(7,1,'Hamburger');
insert into `applied_customization` values(1, 7, 'Doneness', 4);