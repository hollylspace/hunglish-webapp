This file will describe harness options and config files


Running options
--------------------------------
Mandatory options:
-g --graph
    defining graph input file. See description of the file later
-c --commands
    defining commands input file. See description of the file later
-r --root
    Root directory that contains all the text files to be processed

Optional options:        
-d --dryrun
    can be 'normal' or 'brief'. With this no real operations are run, only
    printouts about what would be done while normal running.
    Brief mode is a quiet mode, only a few lines about what to do.
    Normal mode prints everything that is called. This output is a valid shell
    script, could be run later.
-l --lang
    Specify what language the operations are needed in. Root directory structure
    has to follow these names.
    Used only together with '-w' option
-w --what
    Specify what is needed to create.
    Used together with '-l'.
    These two options have to match a TARGET in graph.txt (See later)
-n --norecursion
    Used to stop running after the specified (-l -w) files are done. Without
    stopping all the operations that are dependent from these files are run.
--catalog
    If not all the files found in the root directory we want to run, only a part
    of them we can filter them with catalog file. See catalog file description.
    If no catalog specified a file list generated from the source directory
    (choosing src dir described in graph.txt description later) and this file
    list is used later through the whole progress.
--daemon
    Turning on daemon mode.
    This is used to reduce unnecessary loading of resources all over again.
    There is a lot more to do not only to turn this switch.
    See graph and commands files to see details on how to specify everything.
    If you dont specify a host and a port harness will start daemon itself. Now
    it uses ports started from 10000.

    
Graph file
--------------------------------

Describes dependency graph based on what we want to run after what.
Every line contains one edge of this directed graph.
Lines can be commented with '#'.
Priorities:
    First line has the highest priority, second has the second highest ...
    If a line depends from another line (via sources and targets) then its
    priority is decreased to have lower priority as long as it will have lower
    then all the lines that it's dependent from.
    
    So "source directory" will be the most above line that is not dependent from
    anything else.
One line:
hu/moses, en/moses -> align/giza: giza (collect)
SRCS -> TARGET: CMD (options)
where:
    SRCS:
        list of SRC separated by a comma
        SRC:
            lang/what: this has to meet structure of ROOT directory (see root
                       option)
                lang: language
                what: type of text (pos, lemma, raw, ...)
    TARGET:
        lang/what:
            same as SRC
    CMD:
        name of command. CMD is used to lookup command to run in commands.txt
        (see description in this file)
    options: here we can add extra options. There are two right now:
        daemon:
            actual command running in daemon mode. You can specify if daemon is
            already running or harness has to start it. You have to enable
            DAEMON mode as a harness.py option (see description)
            syntax:
                daemon=host:port if we want to use an external daemon
                daemon           if we want harness to start daemon on localhost
                                 (port range started at 10000)
        collect: collecting mode, all the files found in src folders are
                 concatenated into one file (per source) and command is run only
                 after that. At last, result file is separated based on line
                 count collected on concatenation --> This option is working
                 only when all source and target files are containing one
                 sentence per line.

Examples of edges:
en/pos -> en/np : np_en (daemon)
    np_en does english nps from english pos files via a daemon started by
    harness itself.

en/pos -> en/np : np_en (daemon=localhost:12345)
    np_en does english nps from english pos files via a daemon started by
    user before on localhost:12345.

hu/moses, en/moses -> align/giza: giza (collect)
    giza creates align/giza from hu and en moses files after concatenating all
    files found in ROOT/hu/moses and ROOT/en/moses and after giza has been
    finished result file (temporary file) is separated



Commands file
--------------------------------
Contains all the commands used by harness.
There are two types of variables in the file:
Ending with _cmd suffix:
    These are the actually commands to run. The prefix before _cmd should match
    with one CMD in graph.txt. (Example: CMD=np_hu in graph.txt then np_hu_cmd
                                         should be the variable that describes
                                         the actual command)
All other:
    These options are only for easier rewriting of commands. They are inner
    variables. All these options can be used in all other options with %(s)
    syntax.
    Example:
        option:
            rood_dir: /home/my_home/root
        usage:
            listing_root: ls %(root_dir)s
    Variables can use any other variables, the only limit is maximal depth 
    that python defines.

- lines can be commented with '#'
- a line containing '[commands]' has to prevent all the useful lines so only
    comments can be before
- special syntax: [[text1,text2]]
    this is used to use same commands with different syntax to distinguish
    between daemon and normal mode.
    this string is replaced by text1 if harness is running in normal mode and
                               text2 if running in daemon mode


Catalog file
--------------------------------
Lines:
author<TAB>number
Examples:
Vonnegut    1
subtitles   123