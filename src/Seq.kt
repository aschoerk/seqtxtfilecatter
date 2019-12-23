import java.io.File

/**
 * See in a directory of text files (ending with .txt)
 * Files are to be concatenated
 * either of they start with the same prefix and end with -[0-9][0-9] and the last 2 characters are digits sequential from 1
 * or
 *  they end with -..-[0-9][0-9], the last digists are sequential from 2 and the first file starts with the same prefix.
 *  example: aaa, aaa-ch-02, aaa-ch-03
 *
 * @author aschoerk
 */
fun main(args: Array<String>) {
    for (directoryName in args) {
        var f = File(directoryName);
        val catenated = File(directoryName + "done");
        catenated.mkdir();
        if (f.isDirectory) {
            // make names all textfile-names without .txt, sorted
            var names = f.list()
                .filter({ s -> s.endsWith(".txt") && !s.endsWith(".cat.txt") && s.length > 4 })
                .map({ s -> s.substring(0, s.length - 4) })
                .toSortedSet();

            val discrimRegex = Regex("-[0-9]$")
            // make secondfiles the names of all secondfiles in a group of sequential files
            var secondFiles = names
                .map { n ->
                    if (n.length > 2 && n.substring(0, n.length - 2).matches(discrimRegex)) n.substring(
                        0,
                        n.length - 2
                    ) else n
                }
                .filter { n -> n.endsWith("-02") }
                .map { n -> n.substring(0, n.length - 3) }
                .map { n -> if (n.length > 3 && n[n.length - 3] == '-') n.substring(0, n.length - 3) else n }
            println("number of secondfiles found: " + secondFiles.size.toString())
            var endnumRegex = Regex("-[0-9][0-9]")
            var endnumRegex2 = Regex("-[0-9][0-9]-[0-9]")
            var sortedGroups =
                secondFiles
                    .map { s -> Pair(s, names.subSet(s, s + "z").map { name -> name.substring(s.length) }) }
                    .filter { p -> p.second.size > 1 }
                    .filter { p ->
                        val first = p.second.first()
                        first.isEmpty()
                            || first.matches(discrimRegex)
                            || first.endsWith("01") || first.length == 4 && first.startsWith("01-")
                    }
                    .filter({ p ->
                        var excptfirst = p.second.subList(1, p.second.size)
                        if (excptfirst.all { s ->
                                s.length > 3
                                        && s.substring(s.length - 3).matches(endnumRegex)
                                        ||
                                        s.length > 5
                                        && s.substring(s.length - 5).matches(endnumRegex2)

                            }) {
                            excptfirst
                                .map { s ->
                                    if (s.matches(discrimRegex))
                                        Integer.parseInt(s.substring(s.length - 4, 2))
                                    else Integer.parseInt(s.substring(s.length - 2))
                                }
                                .equals((2..excptfirst.size + 1).toList())
                        } else
                            false
                    })
                    .toMap();


            println("number of sorted groups found: " + sortedGroups.size.toString())
            sortedGroups.entries.forEach({ e ->
                var prefix = directoryName + "/" + e.key
                var content = StringBuffer()
                e.value.forEach { v ->
                    val input = if (v.isEmpty())
                        File(prefix + ".txt")
                    else {
                        File(prefix + v + ".txt")
                    }
                    input.forEachLine { l -> content.append(l).append("\n") }
                    val destName = File(catenated.toPath().toString() + "/" + input.name)
                    val renamed = input.renameTo(destName)
                    if (!renamed) {
                        println("could not rename " + input.name)
                    }
                }
                val destName = prefix + e.value.last() + ".cat.txt"
                println("writing " + destName)
                File(destName).writeText(content.toString(), Charsets.UTF_8);
                println("written " + destName)

            })


        }
    }
}