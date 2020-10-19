const process = require("process");
const postcss = require("postcss")
const tailwindcss = require("tailwindcss");
const fs = require("fs")
const extractClassNames = require("./extractClassNames")

function replacer(key, value) {
    if (key === "css") {
        return null
    }

    return value
}

async function test() {
    let postcssResult

    const tailwindConfig = process.argv[2]
    const tmpFile = process.argv[3]

    try {
        postcssResult = await Promise.all(
            [
                'base',
                'components',
                'utilities',
            ].map((group) =>
                postcss([tailwindcss(tailwindConfig)]).process(`@tailwind ${group};`, {
                    from: undefined,
                })
            )
        )
        const [base, components, utilities] = postcssResult
        const classNames = await extractClassNames([
            { root: base.root, source: 'base' },
            { root: components.root, source: 'components' },
            { root: utilities.root, source: 'utilities' },
        ])

        const json = JSON.stringify(classNames, null)
        fs.writeFileSync(tmpFile, json)
        process.exit()
    } catch (error) {
        throw error
    }
}

test()
