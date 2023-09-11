package com.glist.GroceriesList.model.request;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.glist.GroceriesList.model.groceries.GroceryList;

import java.io.Serializable;
import java.util.List;


//@JsonSerialize(using = ToStringSerializer.class)
public class AddPeopleApiRequestBody implements Serializable {
    public String listId;
    public String containerId;
    public List<String> people;
}
