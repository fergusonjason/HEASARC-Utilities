Array.prototype.unique = function(b) {
    var a = [], i, l = this.length;
    for (i = 0; i < l; i++) {
        if (a.indexOf(this[i], 0, b) < 0) {
            a.push(this[i]);
        }
    }
    return a;
};

db.ppm.update({"_id":{$exists:true}}, {$set:{"xref":[]}}, false, true);
db.ppm.ensureIndex({"dmname":1});

// have to reprocess some of the Durchmusterung names because NASA was lazy
db.ppm.find({dmname:/^(-[0-9]{2})\s+([0-9]*[A-Z]*)$/i}).forEach(function(obj) {
    var firstThree = new Number(obj.dmname.substring(0, 3));
    if (firstThree == "NaN") {
        return;
    }
    if (firstThree < -23) {
        obj.dmname = "COD " + obj.dmname;
    } else {
        obj.dmname = "BD " + obj.dmname;
    }
    db.ppm.save(obj);

});

db.ppm.find({dmname:/^(\+[0-9]{2})\s+([0-9]*[A-Z]*)$/i}).forEach(function(obj) {
    obj.dmname = "BD " + obj.dmname;
    db.ppm.save(obj);
});

db.ppm.find().forEach(function(obj) {
    // star catalogs
    if (obj.cpdname != null) {
        obj.xref.push(obj.cpdname);
    }

    if (obj.dmname != null) {
        obj.xref.push(obj.dmname);
    }
    if (obj.gcname != null) {
        obj.xref.push(obj.gcname);
    }
    if (obj.hdname != null) {
        obj.xref.push(obj.hdname);
    }
    if (obj.ppmname != null) {
        obj.xref.push(obj.ppmname);
    }

    db.ppm.save(obj);
});



db.ppm.find().forEach(function(obj) {
    tempXref = obj.xref.unique();
    obj.xref = tempXref;
    db.ppm.save(obj);
});

db.ppm.update({"bscname":{$exists:true}}, {$unset:{"bscname":1}}, false, true);
db.ppm.update({"hdname":{$exists:true}}, {$unset:{"hdname":1}}, false, true);
db.ppm.update({"saoname":{$exists:true}}, {$unset:{"saoname":1}}, false, true);
db.ppm.update({"ppmname":{$exists:true}}, {$unset:{"ppmname":1}}, false, true);
db.ppm.update({"hipname":{$exists:true}}, {$unset:{"hipname":1}}, false, true);
db.ppm.update({"gcname":{$exists:true}}, {$unset:{"gcname":1}}, false, true);
db.ppm.update({"dmname":{$exists:true}}, {$unset:{"dmname":1}}, false, true);
db.ppm.update({"gliesename":{$exists:true}}, {$unset:{"gliesename":1}}, false, true);
db.ppm.update({"cpdname":{$exists:true}}, {$unset:{"cpdname":1}}, false, true);
db.ppm.update({"messiername":{$exists:true}}, {$unset:{"messiername":1}}, false, true);
db.ppm.update({"ngc2000name":{$exists:true}}, {$unset:{"ngc2000name":1}}, false, true);
db.ppm.update({"ugcname":{$exists:true}}, {$unset:{"ugcname":1}}, false, true);


db.ppm.find({dmname:/^(\+[0-9]{2})\s+([0-9]*[A-Z]*)$/i})


