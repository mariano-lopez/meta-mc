SUMMARY = "Music Player Daemon"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"
HOMEPAGE ="http://www.musicpd.org"

DEPENDS = "alsa-lib boost bzip2 curl dbus expat ffmpeg flac icu libogg libopus libsamplerate0 libsndfile1 libvorbis mpg123 tcp-wrappers wavpack yajl zlib"

SRC_URI = " \
    http://www.musicpd.org/download/${BPN}/0.19/${BP}.tar.xz \
    file://mpd.conf.in \
"
SRC_URI[md5sum] = "d235f6e53e5442b6926c49946a303f8d"
SRC_URI[sha256sum] = "8b3afcd440181c9bd18b229b5974e529d59648344dd371370d6f7d62ec2847c5"

inherit autotools useradd systemd

EXTRA_OECONF = "--enable-database \
                --enable-expat \
                --enable-alsa \
                --enable-zlib \
                --enable-bzip2 \
                --enable-curl \
                --enable-ffmpeg \
                --enable-flac \
                --enable-httpd-output \
                --enable-mpg123 \
                --enable-opus \
                --enable-vorbis \
                --enable-vorbis-encoder \
                --enable-wave-encoder \
                --enable-wavpack \
                --enable-icu \
                --enable-tcp \
                --disable-oss"




EXTRA_OECONF += "${@base_contains('DISTRO_FEATURES', 'systemd', '--with-systemdsystemunitdir=${systemd_unitdir}/system/', '--without-systemdsystemunitdir', d)}"

PACKAGECONFIG[faad2] = "--enable-aac,--disable-aac,faad2"
PACKAGECONFIG[fluidsynth] = "--enable-fluidsynth,--disable-fluidsynth,fluidsynth"
PACKAGECONFIG[id3tag] = "--enable-id3,--disable-id3,libid3tag"
PACKAGECONFIG[jack] = "--enable-jack,--disable-jack,jack"
PACKAGECONFIG[lame] = "--enable-lame-encoder,--disable-lame-encoder,lame"
PACKAGECONFIG[libao] = "--enable-ao,--disable-ao,libao"
PACKAGECONFIG[libcdio] = "--enable-cdio-paranoia,--disable-cdio-paranoia,libcdio"
PACKAGECONFIG[libmms] = "--enable-mms,--disable-mms,libmms"
PACKAGECONFIG[libmodplug] = "--enable-modplug,--disable-modplug,libmodplug"
PACKAGECONFIG[mad] = "--enable-mad,--disable-mad,libmad"
PACKAGECONFIG[openal-soft] = "--enable-openal,--disable-openal,openal-soft"
PACKAGECONFIG[pulse] = "--enable-pulse,--disable-pulse,pulseaudio"
PACKAGECONFIG[sqlite3] = "--enable-sqlite,--disable-sqlite,sqlite3"


do_install_append() {
    install -d ${D}/${localstatedir}/lib/mpd/music
    chmod 775 ${D}/${localstatedir}/lib/mpd/music
    install -d ${D}/${localstatedir}/lib/mpd/playlists
    chown -R mpd ${D}/${localstatedir}/lib/mpd
    chown mpd:mpd ${D}/${localstatedir}/lib/mpd/music

    install -d ${D}/${sysconfdir}
    install -m 644 ${WORKDIR}/mpd.conf.in ${D}/${sysconfdir}/mpd.conf
    sed -i \
        -e 's|%music_directory%|${localstatedir}/lib/mpd/music|' \
        -e 's|%playlist_directory%|${localstatedir}/lib/mpd/playlists|' \
        -e 's|%db_file%|${localstatedir}/lib/mpd/mpd.db|' \
        -e 's|%log_file%|${localstatedir}/log/mpd.log|' \
        -e 's|%state_file%|${localstatedir}/lib/mpd/state|' \
        ${D}/${sysconfdir}/mpd.conf

    if [ -e ${D}/${systemd_unitdir}/system/mpd.service ] ; then
        sed -i \
            's|^ExecStart=.*|ExecStart=${bindir}/mpd --no-daemon|' \
            ${D}/${systemd_unitdir}/system/mpd.service
    fi
}

RPROVIDES_${PN} += "${PN}-systemd"
RREPLACES_${PN} += "${PN}-systemd"
RCONFLICTS_${PN} += "${PN}-systemd"
SYSTEMD_SERVICE_${PN} = "mpd.service mpd.socket"

USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} = " \
    --system --no-create-home \
    --home ${localstatedir}/lib/mpd \
    --groups audio \
    --user-group mpd"
