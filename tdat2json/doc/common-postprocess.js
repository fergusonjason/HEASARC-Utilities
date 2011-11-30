
// just in case a dupe snuck into the xref array somehow
db.catalog.find().forEach(function(obj) {
    tempXref = obj.xref.unique();
    obj.xref = tempXref;
    db.catalog.save(obj);
});

db.catalog.find({ccdmname:{$exists:1}}).forEach(function(obj) {
    if (obj.ccdmname !=null) {
        obj.xref.push(obj.ccdmname);
    }
    db.catalog.save(obj);
});

db.catalog.update({ir_flag:{$exists:1}},{$unset:{ir_flag:1}},false,true);
db.catalog.update({notes:{$exists:1}},{$unset:{notes:1}},false,true);
db.catalog.update({id_chart:{$exists:1}},{$unset:{id_chart:1}},false,true);
db.catalog.update({ads:{$exists:1}},{$unset:{ads:1}},false,true);
db.catalog.update({ads_comp:{$exists:1}},{$unset:{ads_comp:1}},false,true);
db.catalog.update({dmnumber:{$exists:1}},{$unset:{dmnumber:1}},false,true);




