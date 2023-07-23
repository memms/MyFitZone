package com.example.myfitzone.DataModels

import java.sql.Timestamp

data class UserExercise(
    //muscle group
    var exerciseGroup: String,
    //name of exercise
    //also used as key/ID in database
    var name: String,
    //user can save personal exercise notes to view later
    var notes: String,
//    var image: String,
    var fieldmap: HashMap<String, *>,
    var timeAdded: Long   //unix time in seconds
)

/**
 * users    (Collection)
 *     -> UID  (Document)
 *     |        -> userExerciseList (Collection)
 *     |        |       -> Year-Month    (Document)
 *     |        |       |    -> timeAdded (Field) (map)
 *     |        |            |    - exerciseGroup (string)
 *     |        |            |     - name (string)
 *     |        |            |     - notes (string)
 *     |        |            |     - fieldmap (map)
 *     |        |            |     |    - fields (*)
 */

/**
 * userExerciseList (Collection)
 *     -> UID    (Document)
 *     |        -> Year-Month    (Collection)
 *     |        |       -> Week # (Document)
 *     |        |       |    -> timeAdded (Field) (map)
 *     |        |            |    - exerciseGroup (string)
 *     |        |            |     - name (string)
 *     |        |            |     - notes (string)
 *     |        |            |     - fieldmap (map)
 *     |        |            |     |    - fields (*)
 */

/**
 * users    (Collection)
 *      -> UID  (Document)
 *      |        -> userExerciseList (Collection)
 *      |        |       -> exerciseGroup    (Document)
 *      |        |       |    -> name (Field)
 *      |        |            |    - timeAdded (map)
 *      |        |                 |    - notes (string)
 *                                 |    - fieldmap (map)
 */



/**
 * users    (Collection)
 *    -> UID  (Document)
 *    |        -> userExerciseList (Collection)
 *    |        |       -> Year-Month    (Document)
 *    |        |       |    -> timeAdded (Field)
 *
 */
