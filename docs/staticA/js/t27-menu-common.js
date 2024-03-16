'use strict'

async function execFetch(event) {
    console.log("execFetch-BEG")
    let entryUrl = "http://localhost:8070/ext-sedsvc/entry-point/"
    console.log(`entryUrl:${entryUrl}`)

    let response = await fetch(entryUrl)
    console.log(response)

    console.log("execFetch-END")
}