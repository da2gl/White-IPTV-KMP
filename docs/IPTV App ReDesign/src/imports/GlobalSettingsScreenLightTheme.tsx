import svgPaths from "./svg-jiq512xdd8";

function Container7({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0">
      <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex flex-col items-start overflow-clip relative rounded-[inherit]">{children}</div>
    </div>
  );
}

function Container6({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0">
      <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex gap-[8px] items-center relative">{children}</div>
    </div>
  );
}

function Container5({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0">
      <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex gap-[16px] items-center relative">{children}</div>
    </div>
  );
}

function Wrapper1({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="min-h-[72px] relative shrink-0 w-full">
      <div aria-hidden="true" className="absolute border-[#e2e8f0] border-solid border-t inset-0 pointer-events-none" />
      <div className="flex flex-row items-center min-h-[inherit] size-full">{children}</div>
    </div>
  );
}

function HorizontalBorder1({ children }: React.PropsWithChildren<{}>) {
  return (
    <Wrapper1>
      <div className="content-stretch flex items-center justify-between min-h-[inherit] pb-[23.5px] pt-[24.5px] px-[16px] relative w-full">{children}</div>
    </Wrapper1>
  );
}

function Container4({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="h-[16px] relative shrink-0 w-[20px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20 16">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}

function HorizontalBorder({ children }: React.PropsWithChildren<{}>) {
  return (
    <Wrapper1>
      <div className="content-stretch flex items-center justify-between min-h-[inherit] pb-[11.5px] pt-[12.5px] px-[16px] relative w-full">{children}</div>
    </Wrapper1>
  );
}

function Container3({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="min-h-[72px] relative shrink-0 w-full">
      <div className="flex flex-row items-center min-h-[inherit] size-full">
        <div className="content-stretch flex items-center justify-between min-h-[inherit] px-[16px] py-[12px] relative w-full">{children}</div>
      </div>
    </div>
  );
}

function Container2({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0 size-[20px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20 20">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}

function Container1({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0 size-[16px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16 16">
        <g id="Container">{children}</g>
      </svg>
    </div>
  );
}

function Wrapper({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="relative shrink-0 w-full">
      <div className="content-stretch flex flex-col items-start pb-[8px] pt-[16px] px-[16px] relative w-full">
        <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[14px] tracking-[0.7px] uppercase w-full">
          <p className="leading-[20px]">{children}</p>
        </div>
      </div>
    </div>
  );
}

function Container() {
  return (
    <div className="h-[12px] relative shrink-0 w-[7.4px]">
      <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 7.4 12">
        <g id="Container">
          <path d={svgPaths.p28c84800} fill="var(--fill-0, #94A3B8)" id="Icon" />
        </g>
      </svg>
    </div>
  );
}
type HeadingTextProps = {
  text: string;
};

function HeadingText({ text }: HeadingTextProps) {
  return <Wrapper>{text}</Wrapper>;
}

export default function GlobalSettingsScreenLightTheme() {
  return (
    <div className="bg-[#f6f7f8] content-stretch flex flex-col items-start relative size-full" data-name="Global Settings Screen (Light Theme)">
      <div className="content-stretch flex flex-col isolate items-start min-h-[1359px] overflow-clip relative shrink-0 w-full" data-name="Container">
        <div className="bg-[#f6f7f8] relative shrink-0 w-full z-[2]" data-name="Top App Bar">
          <div aria-hidden="true" className="absolute border-[#e2e8f0] border-b border-solid inset-0 pointer-events-none" />
          <div className="flex flex-row items-center size-full">
            <div className="content-stretch flex items-center justify-between pb-[9px] pt-[16px] px-[16px] relative w-full">
              <div className="relative rounded-[9999px] shrink-0 size-[40px]" data-name="Button">
                <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex items-center justify-center relative size-full">
                  <Container1>
                    <path d={svgPaths.p300a1100} fill="var(--fill-0, #334155)" id="Icon" />
                  </Container1>
                </div>
              </div>
              <div className="flex-[1_0_0] min-h-px min-w-px relative" data-name="Heading 1">
                <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex flex-col items-center relative w-full">
                  <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[28px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[18px] text-center w-[73.05px]">
                    <p className="leading-[28px]">Settings</p>
                  </div>
                </div>
              </div>
              <div className="shrink-0 size-[40px]" data-name="Rectangle" />
            </div>
          </div>
        </div>
        <div className="relative shrink-0 w-full z-[1]" data-name="Container">
          <div className="content-stretch flex flex-col gap-[24px] items-start p-[16px] relative w-full">
            <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="APPEARANCE Section">
              <HeadingText text="APPEARANCE" />
              <div className="bg-white content-stretch flex flex-col items-start overflow-clip relative rounded-[24px] shrink-0 w-full" data-name="Background">
                <Container3>
                  <div className="content-stretch flex gap-[16px] items-center relative shrink-0" data-name="Container">
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container2>
                        <path d={svgPaths.p1b1d6580} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container2>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[128.95px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[53.08px]">
                          <p className="leading-[24px]">Theme</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[14px] w-[128.95px]">
                          <p className="leading-[21px]">Light, Dark, System</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div className="content-stretch flex gap-[8px] items-center relative shrink-0" data-name="Container">
                    <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[16px] w-[38.06px]">
                        <p className="leading-[24px]">Light</p>
                      </div>
                    </div>
                    <Container />
                  </div>
                </Container3>
                <HorizontalBorder>
                  <Container5>
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container2>
                        <path d={svgPaths.p2ef76100} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container2>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[99.72px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[99.72px]">
                          <p className="leading-[24px]">Accent Color</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[14px] w-[97.08px]">
                          <p className="leading-[21px]">Teal, Blue, etc.</p>
                        </div>
                      </div>
                    </div>
                  </Container5>
                  <div className="relative shrink-0" data-name="Container">
                    <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex gap-[7.99px] items-center relative">
                      <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[16px] w-[31.88px]">
                          <p className="leading-[24px]">Teal</p>
                        </div>
                      </div>
                      <Container />
                    </div>
                  </div>
                </HorizontalBorder>
                <HorizontalBorder>
                  <Container5>
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container4>
                        <path d={svgPaths.pbfde080} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container4>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[105.17px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[105.17px]">
                          <p className="leading-[24px]">Channel View</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[14px] w-[59.06px]">
                          <p className="leading-[21px]">List, Grid</p>
                        </div>
                      </div>
                    </div>
                  </Container5>
                  <Container6>
                    <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[16px] w-[27.14px]">
                        <p className="leading-[24px]">List</p>
                      </div>
                    </div>
                    <Container />
                  </Container6>
                </HorizontalBorder>
                <HorizontalBorder>
                  <Container5>
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container2>
                        <path d={svgPaths.p237be000} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container2>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[141.19px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[75.7px]">
                          <p className="leading-[24px]">Language</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[14px] w-[141.19px]">
                          <p className="leading-[21px]">English, Spanish, etc.</p>
                        </div>
                      </div>
                    </div>
                  </Container5>
                  <Container6>
                    <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[16px] w-[55.5px]">
                        <p className="leading-[24px]">English</p>
                      </div>
                    </div>
                    <Container />
                  </Container6>
                </HorizontalBorder>
              </div>
            </div>
            <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="PLAYBACK Section">
              <HeadingText text="PLAYBACK" />
              <div className="bg-white content-stretch flex flex-col items-start overflow-clip relative rounded-[24px] shrink-0 w-full" data-name="Background">
                <Container3>
                  <div className="content-stretch flex gap-[16px] items-center relative shrink-0" data-name="Container">
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container2>
                        <path d={svgPaths.p19e3b6c0} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container2>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[111.58px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[107.06px]">
                          <p className="leading-[24px]">Default Player</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[14px] w-[111.58px]">
                          <p className="leading-[21px]">Internal, External</p>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div className="content-stretch flex gap-[8px] items-center relative shrink-0" data-name="Container">
                    <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[16px] w-[57.81px]">
                        <p className="leading-[24px]">Internal</p>
                      </div>
                    </div>
                    <Container />
                  </div>
                </Container3>
                <HorizontalBorder>
                  <Container5>
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container4>
                        <path d={svgPaths.pde75900} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container4>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[166.19px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[130.47px]">
                          <p className="leading-[24px]">Preferred Quality</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[14px] w-[166.19px]">
                          <p className="leading-[21px]">Auto, High, Medium, Low</p>
                        </div>
                      </div>
                    </div>
                  </Container5>
                  <Container6>
                    <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                      <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[16px] w-[35.92px]">
                        <p className="leading-[24px]">Auto</p>
                      </div>
                    </div>
                    <Container />
                  </Container6>
                </HorizontalBorder>
              </div>
            </div>
            <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="APP BEHAVIOR Section">
              <HeadingText text="APP BEHAVIOR" />
              <div className="bg-white content-stretch flex flex-col items-start overflow-clip relative rounded-[24px] shrink-0 w-full" data-name="Background">
                <div className="min-h-[72px] relative shrink-0 w-full" data-name="Container">
                  <div className="flex flex-row items-center min-h-[inherit] size-full">
                    <div className="content-stretch flex items-center justify-between min-h-[inherit] px-[16px] py-[8px] relative w-full">
                      <div className="content-stretch flex gap-[16px] items-center relative shrink-0" data-name="Container">
                        <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                          <div className="h-[15px] relative shrink-0 w-[19px]" data-name="Container">
                            <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 19 15">
                              <g id="Container">
                                <path d={svgPaths.p3eecb600} fill="var(--fill-0, #2BADEE)" id="Icon" />
                              </g>
                            </svg>
                          </div>
                        </div>
                        <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[113.97px]" data-name="Container">
                          <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                            <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[113.97px]">
                              <p className="leading-[24px]">Default Playlist</p>
                            </div>
                          </div>
                          <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                            <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[42px] justify-center leading-[21px] not-italic relative shrink-0 text-[#64748b] text-[14px] w-[109.58px]">
                              <p className="mb-0">Select from your</p>
                              <p>playlists</p>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div className="content-stretch flex gap-[8px] items-center relative shrink-0" data-name="Container">
                        <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                          <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[16px] w-[70.14px]">
                            <p className="leading-[24px]">Favorites</p>
                          </div>
                        </div>
                        <Container />
                      </div>
                    </div>
                  </div>
                </div>
                <HorizontalBorder>
                  <Container5>
                    <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                      <Container1>
                        <path d={svgPaths.p30dddd98} fill="var(--fill-0, #2BADEE)" id="Icon" />
                      </Container1>
                    </div>
                    <div className="content-stretch flex flex-col items-start justify-center relative shrink-0 w-[162.86px]" data-name="Container">
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[162.86px]">
                          <p className="leading-[24px]">Auto Update Playlists</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0 w-full" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[21px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[14px] w-[148.34px]">
                          <p className="leading-[21px]">Update on app startup</p>
                        </div>
                      </div>
                    </div>
                  </Container5>
                  <div className="relative shrink-0" data-name="Container">
                    <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex flex-col items-start relative">
                      <div className="content-stretch flex items-center relative shrink-0" data-name="Label">
                        <div className="bg-[#2badee] h-[24px] rounded-[9999px] shrink-0 w-[44px]" data-name="Background" />
                        <div className="absolute bg-white left-[22px] rounded-[9999px] size-[20px] top-[2px]" data-name="Background+Border">
                          <div aria-hidden="true" className="absolute border border-solid border-white inset-0 pointer-events-none rounded-[9999px]" />
                        </div>
                      </div>
                    </div>
                  </div>
                </HorizontalBorder>
              </div>
            </div>
            <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="DATA & STORAGE Section">
              <Wrapper>{`DATA & STORAGE`}</Wrapper>
              <div className="bg-white content-stretch flex flex-col items-start overflow-clip relative rounded-[24px] shrink-0 w-full" data-name="Background">
                <div className="min-h-[72px] relative shrink-0 w-full" data-name="Container">
                  <div className="flex flex-row items-center min-h-[inherit] size-full">
                    <div className="content-stretch flex items-center min-h-[inherit] px-[16px] py-[12px] relative w-full">
                      <div className="content-stretch flex flex-[1_0_0] gap-[16px] items-center min-h-px min-w-px relative" data-name="Container">
                        <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                          <div className="h-[18px] relative shrink-0 w-[20.85px]" data-name="Container">
                            <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 20.85 18">
                              <g id="Container">
                                <path d={svgPaths.p24fdf900} fill="var(--fill-0, #2BADEE)" id="Icon" />
                              </g>
                            </svg>
                          </div>
                        </div>
                        <div className="content-stretch flex flex-[1_0_0] flex-col items-start min-h-px min-w-px overflow-clip relative" data-name="Container">
                          <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-full">
                            <p className="leading-[24px]">Manage Playlists</p>
                          </div>
                        </div>
                        <Container />
                      </div>
                    </div>
                  </div>
                </div>
                <Wrapper1>
                  <div className="content-stretch flex items-center min-h-[inherit] pb-[11.5px] pt-[12.5px] px-[16px] relative w-full">
                    <div className="flex-[1_0_0] min-h-px min-w-px relative" data-name="Container">
                      <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex gap-[16px] items-center relative w-full">
                        <div className="bg-[rgba(43,173,238,0.2)] content-stretch flex items-center justify-center relative rounded-[16px] shrink-0 size-[48px]" data-name="Overlay">
                          <div className="h-[18px] relative shrink-0 w-[16px]" data-name="Container">
                            <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 16 18">
                              <g id="Container">
                                <path d={svgPaths.p2780bd80} fill="var(--fill-0, #2BADEE)" id="Icon" />
                              </g>
                            </svg>
                          </div>
                        </div>
                        <div className="content-stretch flex flex-[1_0_0] flex-col items-start min-h-px min-w-px overflow-clip relative" data-name="Container">
                          <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium justify-center leading-[0] not-italic relative shrink-0 text-[#ef4444] text-[16px] w-full">
                            <p className="leading-[24px]">Clear Cache</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </Wrapper1>
              </div>
            </div>
            <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="ABOUT Section">
              <HeadingText text="ABOUT" />
              <div className="bg-white content-stretch flex flex-col items-start overflow-clip relative rounded-[24px] shrink-0 w-full" data-name="Background">
                <div className="min-h-[72px] relative shrink-0 w-full" data-name="Container">
                  <div className="flex flex-row items-center min-h-[inherit] size-full">
                    <div className="content-stretch flex items-center justify-between min-h-[inherit] px-[16px] py-[24px] relative w-full">
                      <div className="content-stretch flex flex-col items-start overflow-clip relative shrink-0" data-name="Container">
                        <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[93.41px]">
                          <p className="leading-[24px]">App Version</p>
                        </div>
                      </div>
                      <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                        <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[16px] w-[35.28px]">
                          <p className="leading-[24px]">1.4.2</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <HorizontalBorder1>
                  <Container7>
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[107.22px]">
                      <p className="leading-[24px]">Privacy Policy</p>
                    </div>
                  </Container7>
                  <Container />
                </HorizontalBorder1>
                <HorizontalBorder1>
                  <Container7>
                    <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-[130.13px]">
                      <p className="leading-[24px]">Terms of Service</p>
                    </div>
                  </Container7>
                  <Container />
                </HorizontalBorder1>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}