db.catalog.ensureIndex({"dmname":1});

// have to reprocess some of the Durchmusterung names because NASA was lazy
db.catalog.find({dmname:/^(-[0-9]{2})\s+([0-9]*[A-Z]*)$/i}).forEach(function(obj) {
    var firstThree = new Number(obj.dmname.substring(0, 3));
    if (firstThree == "NaN") {
        return;
    }
    // according to the description page, anything below -23 degrees is a CD from the Cordoba observatory, otherwise
    // it's a BD number
    if (firstThree < -23) {
        obj.dmname = "CD " + obj.dmname;
    } else {
        obj.dmname = "BD " + obj.dmname;
    }
    db.catalog.save(obj);

});

db.catalog.find({dmname:/^(\+[0-9]{2})\s+([0-9]*[A-Z]*)$/i}).forEach(function(obj) {
    obj.dmname = "BD " + obj.dmname;
    db.catalog.save(obj);
});

db.catalog.find({dmname:/-([0-9]{2})([0-9]+[A-Z])$/i}).forEach(function(obj) {
    var dmArray = obj.dmname.match(/-([0-9]{2})([0-9]+[A-Z])$/i);
    if (dmArray.length > 0) {
        var prefix = new Number(dmArray[1]);
        if (prefix < -23) {
            obj.dmname = "CD-" + prefix + " " + dmArray[2];
        } else {
            if (prefix < 0) {
                obj.dmname = "BD-" + prefix + " " + dmArray[2];
            } else {
                obj.dmname = "BD+" + prefix + " " + dmArray[2];
            }
        }
        db.catalog.save(obj);
    }
});

db.catalog.find().forEach(function(obj) {
    // star catalogs
    if (obj.dmname != null) {
        obj.xref.push(obj.dmname);
    }

    db.catalog.save(obj);
});


db.catalog.update({"dmname":{$exists:true}}, {$unset:{"dmname":1}}, false, true);






