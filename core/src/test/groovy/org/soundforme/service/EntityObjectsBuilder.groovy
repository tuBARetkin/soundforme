package org.soundforme.service

import org.soundforme.model.Release
import org.soundforme.model.Subscription
import org.soundforme.model.SubscriptionType
import org.soundforme.model.Track

import java.time.LocalDateTime

import static java.util.UUID.randomUUID

/**
 * @author NGorelov
*/
class EntityObjectsBuilder {

    static def createRandomRelease(id){
        createRandomRelease(id, false, false)
    }

    static def createRandomRelease(id, starred, checked){
        new Release([
                discogsId: id,
                artist: randomUUID(),
                title: randomUUID(),
                releaseDate: "2015",
                collectedDate: LocalDateTime.now(),
                label: randomUUID(),
                catNo: randomUUID(),
                checked: checked,
                starred: starred,
                trackList: [
                        new Track([title: randomUUID(), position: "A1", duration: "5:10"]),
                        new Track([title: randomUUID()])
                ]
        ])
    }

    static def createRandomSubscription(labelNeeded, discogsId, closed){
        new Subscription([
                title: randomUUID(),
                discogsId: discogsId,
                type: labelNeeded ? SubscriptionType.LABEL : SubscriptionType.ARTIST,
                closed: closed
        ])
    }
}
