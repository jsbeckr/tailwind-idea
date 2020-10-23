const process = require("process");
const resolveFrom = require("resolve-from")
const importFrom = require("import-from")
const fs = require("fs")
const path = require("path")
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
    const cwd = process.argv[4]
    const nodeModulesPath = `${cwd}/node_modules`

    const postcss = importFrom(nodeModulesPath, './postcss')
    const tailwindcss = importFrom(nodeModulesPath, './tailwindcss')

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
            {root: base.root, source: 'base'},
            {root: components.root, source: 'components'},
            {root: utilities.root, source: 'utilities'},
        ])

        const json = JSON.stringify(classNames, null)
        fs.writeFileSync(tmpFile, json)
        process.exit()
    } catch (error) {
        throw error
    }
}

test()
