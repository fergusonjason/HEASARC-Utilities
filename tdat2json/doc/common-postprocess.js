
// just in case a dupe snuck into the xref array somehow
db.catalog.find().forEach(function(obj) {
    tempXref = obj.xref.unique();
    obj.xref = tempXref;
    db.catalog.save(obj);
});